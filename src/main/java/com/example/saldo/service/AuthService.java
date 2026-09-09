package com.example.saldo.service;

import com.example.saldo.dto.auth.LoginResult;
import com.example.saldo.entity.RefreshSession;
import com.example.saldo.entity.User;
import com.example.saldo.repository.RefreshSessionRepository;
import com.example.saldo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final RefreshSessionRepository refreshSessionRepository;

    @Transactional
    public LoginResult login(
            String login,
            String password
    ) {
        User user = userRepository
                .findByLogin(login)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid login or password"
                        )
                );

        boolean passwordValid = passwordEncoder.matches(
                password,
                user.getPasswordHash()
        );

        if (!passwordValid) {
            throw new BadCredentialsException(
                    "Invalid login or password"
            );
        }

        String accessToken =
                jwtService.createAccessToken(user);

        UUID sessionId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();

        Instant expiresAt =
                Instant.now()
                        .plus(Duration.ofDays(30));

        RefreshSession refreshSession =
                RefreshSession.builder()
                        .id(sessionId)
                        .user(user)
                        .familyId(familyId)
                        .expiresAt(expiresAt)
                        .build();

        refreshSessionRepository.save(refreshSession);

        String refreshToken =
                jwtService.createRefreshToken(
                        user,
                        sessionId,
                        expiresAt
                );

        return new LoginResult(
                accessToken,
                refreshToken
        );
    }

    @Transactional
    public LoginResult refresh(String refreshToken) {

        Jwt jwt = jwtService.decodeRefreshToken(
                refreshToken
        );

        String tokenType =
                jwt.getClaimAsString(
                        "token_type"
                );

        if (!"refresh".equals(tokenType)) {
            throw new BadCredentialsException(
                    "Invalid refresh token"
            );
        }

        UUID sessionId;

        try {
            sessionId = UUID.fromString(jwt.getId());
        } catch (Exception e) {
            throw new BadCredentialsException(
                    "Invalid refresh token"
            );
        }

        RefreshSession oldSession =
                refreshSessionRepository
                        .findByIdForUpdate(sessionId)
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        "Refresh session not found"
                                )
                        );

        UUID tokenUserId;

        try {
            tokenUserId =
                    UUID.fromString(jwt.getSubject());
        } catch (Exception e) {
            throw new BadCredentialsException(
                    "Invalid refresh token"
            );
        }

        if (!oldSession.getUser().getId().equals(tokenUserId)) {
            throw new BadCredentialsException(
                    "Invalid refresh token"
            );
        }

        if (oldSession.getRevokedAt() != null) {
            refreshSessionRepository.revokeFamily(
                    oldSession.getFamilyId(),
                    Instant.now()
            );

            throw new BadCredentialsException(
                    "Refresh token reuse detected"
            );
        }

        Instant now = Instant.now();
        if (oldSession.getExpiresAt()
                .isBefore(now)) {

            throw new BadCredentialsException(
                    "Refresh token expired"
            );
        }

        User user = oldSession.getUser();

        oldSession.setRevokedAt(now);

        UUID newSessionId = UUID.randomUUID();

        Instant newExpiresAt = now.plus(Duration.ofDays(30));

        RefreshSession newSession =
                RefreshSession.builder()
                        .id(newSessionId)
                        .user(user)
                        .familyId(
                                oldSession.getFamilyId()
                        )
                        .expiresAt(newExpiresAt)
                        .build();

        oldSession.setReplacedBy(newSessionId);

        refreshSessionRepository.save(
                oldSession
        );

        refreshSessionRepository.save(
                newSession
        );

        String access =
                jwtService.createAccessToken(user);

        String refresh =
                jwtService.createRefreshToken(
                        user,
                        newSessionId,
                        newExpiresAt
                );

        return new LoginResult(
                access,
                refresh
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            Jwt jwt =
                    jwtService.decodeRefreshToken(
                            refreshToken
                    );

            if(!"refresh".equals(
                    jwt.getClaimAsString("token_type")
            )) {
                return;
            }

            UUID sessionId =
                    UUID.fromString(jwt.getId());

            refreshSessionRepository
                    .findById(sessionId)
                    .ifPresent(session ->
                            refreshSessionRepository.
                                    revokeFamily(
                                            session.getFamilyId(),
                                            Instant.now()
                                    )
                            );
        } catch (Exception ignor){}
    }
}
