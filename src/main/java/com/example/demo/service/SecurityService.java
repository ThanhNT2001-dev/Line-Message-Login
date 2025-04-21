package com.example.demo.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.example.demo.domain.User;
import com.example.demo.domain.response.auth.ResUserTokenDTO;

@Service
public class SecurityService {
    private final JwtEncoder jwtEncoder;

    public SecurityService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    private static final String USER_PAYLOAD = "user";

    @Value("${tomosia.jwt.secret-key}")
    private String jwtKey;

    @Value("${tomosia.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${tomosia.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public String createAccessToken(String email, User user) {
        // token save information: id, name, email
        ResUserTokenDTO userTokenDTO = new ResUserTokenDTO();
        userTokenDTO.setId(user.getId());
        userTokenDTO.setName(user.getName());
        userTokenDTO.setEmail(user.getEmail());

        // set the expiration time of the JWT access_token
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);


        // Header: only stores algorithm
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        // Payload: JWT claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim(USER_PAYLOAD, userTokenDTO) // User info payload
                .claim("authorities", "ROLE_USER") // Role-based authority
                .build();

        // Signature: (Header + Payload) +  algorithm (jwsHeader -> MacAlgorithm.HS512) + Secret Key
        // Secret Key (configured from application.yml) then injected into JwtEncoder
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createRefreshToken(String email, User user) {
        // token save information: id, name, email
        ResUserTokenDTO userTokenDTO = new ResUserTokenDTO();
        userTokenDTO.setId(user.getId());
        userTokenDTO.setName(user.getName());
        userTokenDTO.setEmail(user.getEmail());

        // set the expiration time of the JWT refresh_token
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        // Header: only stores algorithm
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        // Payload: JWT claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim(USER_PAYLOAD, userTokenDTO) // User info payload
                .build();

        // Signature: (Header + Payload) +  algorithm (jwsHeader -> MacAlgorithm.HS512) + Secret Key
        // Secret Key (configured from application.yml) then injected into JwtEncoder
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
