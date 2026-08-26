package com.example.saldo.controller;

import com.example.saldo.entity.User;
import com.example.saldo.repository.UserRepository;
import com.example.saldo.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    private final RegistrationService registrationService;

    @GetMapping
    public Optional<User> getUser(@RequestParam("email") String email) {
        return userRepository.getUserByEmail(email);
    }

    @PostMapping
    public ResponseEntity<User> registerUser(
            @RequestParam("login") String login,
            @RequestParam("email") String email,
            @RequestParam("password") String password) {

        if(userRepository.existsByEmail(email)) {
            return ResponseEntity.
                    status(HttpStatus.CONFLICT)
                    .build();
        }

        Optional<User> user = registrationService.registration(login, email, password);

        return user.map(value -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(value)).orElseGet(() -> ResponseEntity
                .status(HttpStatus.CONFLICT)
                .build());

    }

}
