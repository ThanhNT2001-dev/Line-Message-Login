package com.example.demo.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SecurityService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

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
    private final UserRepository userRepository;
    private final WebClient webClient = WebClient.create();

    public LineLoginController(UserService userService, ObjectMapper objectMapper, SecurityService securityService, UserRepository userRepository) {
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.securityService = securityService;
        this.userRepository = userRepository;
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
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestParam("code") String code) throws IOException {
        // 1. Exchange code for access_token & id_token
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        String tokenResponseStr = webClient.post()
                .uri("https://api.line.me/oauth2/v2.1/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode tokenResponse = objectMapper.readTree(tokenResponseStr);
        String accessToken = tokenResponse.get("access_token").asText();
        String idToken = tokenResponse.get("id_token").asText();

        // 2. Decode id_token to extract email (if permission granted)
        String email = null;
        String[] parts = idToken.split("\\.");
        if (parts.length == 3) {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payloadJson = objectMapper.readTree(payload);
            JsonNode emailNode = payloadJson.get("email");
            if (emailNode != null) {
                email = emailNode.asText();
            }
        }
        if (email == null || email.isEmpty()) {
            email = UUID.randomUUID().toString() + "@line.com"; // fallback if no email
        }

        // 3. Get user profile info from LINE
        String profileStr = webClient.get()
                .uri("https://api.line.me/v2/profile")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode profileJson = objectMapper.readTree(profileStr);
        String userId = profileJson.has("userId") ? profileJson.get("userId").asText() : "unknown";
        String displayName = profileJson.has("displayName") ? profileJson.get("displayName").asText() : "Unknown";
        String pictureUrl = profileJson.has("pictureUrl") ? profileJson.get("pictureUrl").asText() : "";

        // 4. Create or retrieve user from DB
        User user = this.userService.handleGetUserByUsername(email);
        if (user == null) {
            User newUser = new User();
            newUser.setName(displayName);
            newUser.setEmail(email);
            newUser.setAvatar(pictureUrl);
            newUser.setPassword("");
            newUser.setCreateAt(Instant.now());
            newUser.setPhone(""); // avoid validation issue
            newUser.setLineId(userId);

            user = this.userService.handleCreateUser(newUser);
        } else {
            String existingAvatar = user.getAvatar();
            if (pictureUrl != null && !pictureUrl.isEmpty() && !pictureUrl.equals(existingAvatar)) {
                user.setAvatar(pictureUrl);
                user.setUpdatedAt(Instant.now());
                this.userRepository.save(user);   // Update DB
            }
        }

        // 5. Generate JWT tokens
        String accessJwt = this.securityService.createAccessToken(user.getEmail(), user);
        String refreshJwt = this.securityService.createRefreshToken(user.getEmail(), user);
        this.userService.updateUserToken(refreshJwt, user.getEmail());

        // 6. Create refresh_token cookie
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshJwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        // 7. Return response
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("access_token", accessJwt);
        responseData.put("user", Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "avatar", user.getAvatar(),
                "lineId", user.getLineId()));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseData);
    }

}
