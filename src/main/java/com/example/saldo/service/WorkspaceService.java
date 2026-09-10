package com.example.saldo.service;

import com.example.saldo.dto.workspace.*;
import com.example.saldo.entity.*;
import com.example.saldo.exception.ConflictException;
import com.example.saldo.exception.NotFoundException;
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

    @Transactional
    public CategoryResponse createCategory(UUID userId, UUID workspaceId, CategoryRequest categoryRequest) throws AccessDeniedException {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Workspace не найден")
                );

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException("Нет прав на данный workspace");
        }

        String name = categoryRequest.name().trim();

        if (categoryRepository.existsByWorkspaceIdAndNameIgnoreCaseAndType(
                workspaceId,
                name,
                categoryRequest.type()
        )) {
            throw new ConflictException("Данная категория уже существует");
        }

        Category category = Category.builder()
                .name(name)
                .type(categoryRequest.type())
                .workspace(workspace)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return new CategoryResponse(
                savedCategory.getId(),
                savedCategory.getName(),
                savedCategory.getType()
        );
    }

    public PageResponse<TransactionResponse> getTransactions(
            UUID userId,
            UUID workspaceId,
            int page,
            int size
    ) throws AccessDeniedException {
        boolean hasAccess = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);

        if (!hasAccess) {
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

    @Transactional
    public CategoryResponse updateCategory(
            UUID userId,
            UUID workspaceId,
            UUID categoryId,
            UpdateCategoryRequest categoryRequest
    ) throws AccessDeniedException {
        if (categoryRequest.name() == null && categoryRequest.type() == null) {
            throw new IllegalArgumentException("Нужно передать name или type");
        }

        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        if (!workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException(
                    "Нет прав на данный workspace"
            );
        }

        Category category = categoryRepository
                .findByIdAndWorkspaceId(categoryId, workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Нет такой категории")
                );

        String newName = categoryRequest.name() != null
                ? categoryRequest.name().trim()
                : category.getName();
        CategoryType newType = categoryRequest.type() != null
                ? categoryRequest.type()
                : category.getType();

        if (newName.isBlank()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }

        boolean nameChanged = !newName.equalsIgnoreCase(category.getName())
                || !newType.equals(category.getType());
        if (nameChanged && categoryRepository.existsByWorkspaceIdAndNameIgnoreCaseAndType(
                workspaceId, newName, newType)) {
            throw new ConflictException("Данная категория уже существует");
        }

        category.setName(newName);
        category.setType(newType);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getType()
        );
    }

    @Transactional
    public void deleteCategory(
            UUID userId,
            UUID workspaceId,
            UUID categoryId
    ) throws AccessDeniedException {
        if (!workspaceRepository.existsById(workspaceId)) {
            throw new NotFoundException("Workspace не найден");
        }

        if (!workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new AccessDeniedException(
                    "Нет прав на данный workspace"
            );
        }

        Category category = categoryRepository
                .findByIdAndWorkspaceId(categoryId, workspaceId)
                .orElseThrow(() ->
                        new NotFoundException("Нет такой категории")
                );

        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException(
                    "Категория используется в транзакциях и не может быть удалена"
            );
        }

        categoryRepository.delete(category);
    }
}
