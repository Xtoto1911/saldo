package com.example.saldo.service;

import com.example.saldo.entity.*;
import com.example.saldo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;

    private final WorkspaceRepository workspaceRepository;

    private final WorkspaceMemberRepository workspaceMemberRepository;

    private final WalletRepository walletRepository;

    private final WorkspaceWalletRepository workspaceWalletRepository;

    @Transactional
    public Optional<User> registration(String login, String email, String password) {

        User user = User.builder()
                .login(login)
                .email(email)
                .passwordHash(password)
                .build();

        userRepository.save(user);

        Workspace workspace = Workspace.builder().name("Личное").build();

        workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(Role.OWNER)
                .build();

        workspaceMemberRepository.save(workspaceMember);

        Wallet wallet = Wallet.builder()
                .name("Наличные")
                .owner(user)
                .currency("RUB")
                .initialBalance(BigDecimal.ZERO)
                .build();

        walletRepository.save(wallet);

        WorkspaceWallet workspaceWallet = WorkspaceWallet.builder()
                .wallet(wallet)
                .workspace(workspace)
                .build();

        workspaceWalletRepository.save(workspaceWallet);

        return Optional.of(user);
    }
}
