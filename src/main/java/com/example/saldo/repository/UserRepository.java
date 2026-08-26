package com.example.saldo.repository;


import com.example.saldo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> getUserByEmail(String email);

    boolean existsByEmail(String email);
}
