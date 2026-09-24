package com.example.saldo.controller;

import com.example.saldo.dto.auth.AccessTokenResponse;
import com.example.saldo.dto.auth.LoginRequest;
import com.example.saldo.dto.auth.LoginResult;
import com.example.saldo.dto.auth.RegisterRequest;
import com.example.saldo.entity.User;
import com.example.saldo.repository.UserRepository;
import com.example.saldo.service.AuthService;
import com.example.saldo.service.RefreshCookieService;
import com.example.saldo.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    private final RegistrationService registrationService;

    private final AuthService authService;

    private final RefreshCookieService refreshCookieService;

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(
            @RequestBody LoginRequest request
    ) {
        LoginResult result =
                authService.login(
                        request.login(),
                        request.password()
                );

        ResponseCookie cookie =
                refreshCookieService.create(
                        result.refreshToken()
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        new AccessTokenResponse(result.accessToken())
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @CookieValue(
                    name = "refresh_token",
                    required = false
            )
            String refreshToken
    ) {

        if (refreshToken == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        LoginResult result = authService.refresh(refreshToken);

        ResponseCookie cookie =
                refreshCookieService.create(
                        result.refreshToken()
                );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(
                        new AccessTokenResponse(
                                result.accessToken()
                        )
                );
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @RequestBody RegisterRequest registerRequest
    ) {

        if (userRepository.existsByEmail(registerRequest.email())) {
            return ResponseEntity.
                    status(HttpStatus.CONFLICT)
                    .build();
        }

        if (userRepository.existsByLogin(registerRequest.login())) {
            return ResponseEntity.
                    status(HttpStatus.CONFLICT)
                    .build();
        }

        Optional<User> user = registrationService.registration(
                registerRequest.login(),
                registerRequest.email(),
                registerRequest.password()
        );

        return user.map(value -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(value)).orElseGet(() -> ResponseEntity
                .status(HttpStatus.CONFLICT)
                .build());

    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(
                    name = "refresh_token",
                    required = false
            )
            String refreshToken
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie clearCookie =
                refreshCookieService.clear();

        return ResponseEntity
                .noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        clearCookie.toString()
                )
                .build();
    }

}
