package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.domain.User;
import org.springframework.http.HttpHeaders;
import com.example.demo.domain.request.auth.RequestLoginDTO;
import com.example.demo.domain.response.auth.ResLoginDTO;
import com.example.demo.service.SecurityService;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private static final String REFRESH_TOKEN = "refresh_token";

     private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityService securityService;
    private final UserService userService;

    @Value("${tomosia.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityService securityService, UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityService = securityService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody RequestLoginDTO loginDTO) {
        // Load input including username/password into Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());

        // authenticate user =>  write loadUserByUsername function: Check if the login information is valid
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // Set information to SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User currentUser = this.userService.handleGetUserByUsername(loginDTO.getUsername());

        // create access_token
        String accessToken = this.securityService.createAccessToken(authentication.getName(), currentUser);

        // Format response access_token
        ResLoginDTO res = new ResLoginDTO(accessToken);

        // create refresh_token
        String refreshToken = this.securityService.createRefreshToken(loginDTO.getUsername(), currentUser);

        // Save refresh_token to database
        this.userService.updateUserToken(refreshToken, loginDTO.getUsername());

        // Create cookies
        ResponseCookie resCookies = ResponseCookie
                .from(REFRESH_TOKEN, refreshToken)  // Save refresh_token to Cookie
                .httpOnly(true)                              // Block JavaScript from accessing Cookies (anti-XSS protection)
                .secure(true)                                // Only send Cookies over HTTPS
                .path("/")                                   // Cookies are valid in all domains.
                .maxAge(refreshTokenExpiration)              // Cookie Expiration (refreshTokenExpiration)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(res);
    }
}
