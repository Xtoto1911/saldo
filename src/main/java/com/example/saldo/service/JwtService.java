package com.example.saldo.service;

import com.example.saldo.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder accessJwtEncoder;
    private final JwtEncoder refreshJwtEncoder;
    private final JwtDecoder refreshJwtDecoder;

    public JwtService(
            @Qualifier("accessJwtEncoder")
            JwtEncoder accessJwtEncoder,

            @Qualifier("refreshJwtEncoder")
            JwtEncoder refreshJwtEncoder,

            @Qualifier("refreshJwtDecoder")
            JwtDecoder refreshJwtDecoder
    ) {
        this.accessJwtEncoder = accessJwtEncoder;
        this.refreshJwtEncoder = refreshJwtEncoder;
        this.refreshJwtDecoder = refreshJwtDecoder;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("saldo")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(10)))
                .claim("token_type", "access")
                .build();

        return accessJwtEncoder
                .encode(
                        JwtEncoderParameters.from(claims)
                )
                .getTokenValue();
    }

    public String createRefreshToken(
            User user,
            UUID sessionId,
            Instant expiresAt
    ) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("saldo")
                .subject(user.getId().toString())
                .id(sessionId.toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("token_type", "refresh")
                .build();
        return refreshJwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }


    public Jwt decodeRefreshToken(String token) {
        return refreshJwtDecoder.decode(token);
    }
}
