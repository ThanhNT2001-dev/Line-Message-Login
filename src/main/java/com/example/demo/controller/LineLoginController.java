package com.example.demo.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.domain.User;
import com.example.demo.service.SecurityService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/login/line")
public class LineLoginController {
    @Value("${line.login.client-id}")
    private String clientId;

    @Value("${line.login.client-secret}")
    private String clientSecret;

    @Value("${line.login.redirect-uri}")
    private String redirectUri;

    @Value("${tomosia.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final SecurityService securityService;
    private final WebClient webClient = WebClient.create();

    public LineLoginController(UserService userService, ObjectMapper objectMapper, SecurityService securityService) {
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.securityService = securityService;
    }

    @GetMapping
    public void redirectToLine(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();
        String url = "https://access.line.me/oauth2/v2.1/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&state=" + state +
                "&scope=profile%20openid%20email";

        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<String>> handleCallback(@RequestParam("code") String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        return webClient.post()
                .uri("https://api.line.me/oauth2/v2.1/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(tokenResponseStr -> {
                    try {
                        JsonNode tokenResponse = objectMapper.readTree(tokenResponseStr);
                        String accessToken = tokenResponse.get("access_token").asText();

                        return webClient.get()
                                .uri("https://api.line.me/v2/profile")
                                .headers(headers -> headers.setBearerAuth(accessToken))
                                .retrieve()
                                .bodyToMono(String.class)
                                .flatMap(profileStr -> {
                                    try {
                                        JsonNode profileJson = objectMapper.readTree(profileStr);
                                        String userId = profileJson.get("userId").asText();
                                        String displayName = profileJson.get("displayName").asText();
                                        String pictureUrl = profileJson.get("pictureUrl").asText();

                                        System.out.println("LINE userId: " + userId);

                                        // Tạo user giả định
                                        String email = userId + "@line.com";
                                        User user = this.userService.handleGetUserByUsername(email);
                                        if (user == null) {
                                            User newUser = new User();
                                            newUser.setName(displayName);
                                            newUser.setEmail(email);
                                            newUser.setAvatar(pictureUrl);
                                            newUser.setPassword("123456");
                                            newUser.setCreateAt(Instant.now());

                                            user = userService.handleCreateUser(newUser);

                                            System.out.println("Created new user with email = " + user.getEmail());
                                        }

                                        // Tạo JWT token
                                        String accessJwt = securityService.createAccessToken(user.getEmail(), user);
                                        String refreshJwt = securityService.createRefreshToken(user.getEmail(), user);

                                        // Lưu refresh_token
                                        userService.updateUserToken(refreshJwt, user.getEmail());

                                        // Gửi refresh_token qua cookie
                                        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshJwt)
                                                .httpOnly(true)
                                                .secure(true)
                                                .path("/")
                                                .maxAge(refreshTokenExpiration)
                                                .build();

                                        return Mono.just(ResponseEntity.status(302)
                                                .header(HttpHeaders.LOCATION,
                                                        "http://localhost:5173/user-detail?token=" + accessJwt)
                                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                                .build());
                                    } catch (Exception e) {
                                        return Mono.error(new RuntimeException("Error parsing LINE profile", e));
                                    }
                                });
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error parsing LINE token response", e));
                    }
                });
    }
}
