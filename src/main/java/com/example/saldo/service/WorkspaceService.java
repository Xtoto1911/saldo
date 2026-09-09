package com.example.saldo.service;

import com.example.saldo.dto.workspace.*;
import com.example.saldo.entity.*;
import com.example.saldo.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    private final WalletRepository walletRepository;

    private final UserRepository userRepository;

    private final WorkspaceWalletRepository workspaceWalletRepository;

    private final WorkspaceMemberRepository workspaceMemberRepository;

    private final CategoryRepository categoryRepository;

    private final TransactionRepository transactionRepository;

    public List<WorkspaceResponse> getAllWorkspaces(UUID userId) {
        return workspaceRepository.findAllByUserId(userId).stream()
                .map(workspace -> new WorkspaceResponse(
                        workspace.getId(),
                        workspace.getName()
                ))
                .toList();
    }

    public List<WalletResponse> getWorkspaceWallets(UUID workspaceId) {
        return walletRepository.findWalletsWithBalanceByWorkspaceId(workspaceId).stream()
                .map(wallet -> new WalletResponse(
                        wallet.getId(),
                        wallet.getName(),
                        wallet.getCurrency(),
                        wallet.getBalance()
                ))
                .toList();
    }

    @Transactional
    public WalletResponse createWallet(
            UUID userId,
            UUID workspaceId,
            WalletRequest walletRequest
    ) throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found")
                );

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Workspace not found")
                );

        boolean isMember =
                workspaceMemberRepository
                        .existsByWorkspaceIdAndUserId(
                                workspaceId,
                                userId
                        );

        if (!isMember) {
            throw new AccessDeniedException(
                    "User has no access to workspace"
            );
        }

        Wallet wallet = Wallet.builder()
                .name(walletRequest.name())
                .owner(user)
                .initialBalance(walletRequest.initialBalance())
                .currency("RUB")
                .build();

        Wallet createdWallet = walletRepository.save(wallet);

        WorkspaceWallet workspaceWallet =
                WorkspaceWallet.builder()
                        .workspace(workspace)
                        .wallet(createdWallet)
                        .build();

        workspaceWalletRepository.save(workspaceWallet);

        return new WalletResponse(
                createdWallet.getId(),
                createdWallet.getName(),
                createdWallet.getCurrency(),
                createdWallet.getInitialBalance()
        );
    }

    public List<CategoryResponse> getAllCategories(UUID workspaceId) {
        return categoryRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getType()
                ))
                .toList();
    }

    public PageResponse<TransactionResponse> getTransactions(
            UUID userId,
            UUID workspaceId,
            int page,
            int size
    ) throws AccessDeniedException {
        boolean hasAccess = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);

        if(!hasAccess) {
            throw new AccessDeniedException(
                    "User has no access to workspace"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "occurredAt"
                )
        );

        Page<Transaction> transactions = transactionRepository.findAllByWorkspace_Id(
                workspaceId,
                pageable
        );

        Page<TransactionResponse> response =
                transactions.map(transaction ->
                        new TransactionResponse(
                                transaction.getId(),
                                transaction.getWallet().getId(),
                                transaction.getWallet().getName(),
                                transaction.getCategory().getName(),
                                transaction.getCategory().getType(),
                                transaction.getAmount(),
                                transaction.getOccurredAt(),
                                transaction.getComment()
                        )
                );

        return new PageResponse<>(
                response.getContent(),
                response.getTotalElements(),
                response.getTotalPages(),
                response.getNumber()
        );
    }
}
