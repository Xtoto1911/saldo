package com.example.saldo.controller;

import com.example.saldo.dto.workspace.*;
import com.example.saldo.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getUserWorkspace(
            @AuthenticationPrincipal Jwt jwt
    ) {
        //сделать проверку на null jwt.getSubject()
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(workspaceService.getAllWorkspaces(userId));
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WorkspaceRequest workspaceRequest
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.createWorkspace(userId, workspaceRequest));
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<MemberResponse>> getMembers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId
    ) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(workspaceService.getMembers(userId, workspaceId));
    }

    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<MemberResponse> inviteMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody InviteRequest inviteRequest
    ) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.inviteMember(userId, workspaceId, inviteRequest));
    }

    @GetMapping("/{workspaceId}/wallets")
    public ResponseEntity<List<WalletResponse>> getWorkspaceWallet(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId) {
        //сделать проверку на null
        UUID useId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(workspaceService.getWorkspaceWallets(workspaceId));
    }

    @PostMapping("/{workspaceId}/wallets")
    public ResponseEntity<WalletResponse> createWalletForWorkspace(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody WalletRequest walletRequest) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());
        WalletResponse wallet = workspaceService.createWallet(userId, workspaceId, walletRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @PutMapping("/{workspaceId}/wallets/{walletId}")
    public ResponseEntity<WalletResponse> updateWallet(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("walletId") UUID walletId,
            @Valid @RequestBody UpdateWalletRequest walletRequest) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                workspaceService.updateWallet(userId, workspaceId, walletId, walletRequest)
        );
    }

    @PostMapping("/{workspaceId}/wallets/attach")
    public ResponseEntity<WalletResponse> attachWallet(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody AttachWalletRequest attachRequest) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                workspaceService.attachWallet(userId, workspaceId, attachRequest)
        );
    }

    @GetMapping("/{workspaceId}/categories")
    public ResponseEntity<List<CategoryResponse>> getCategoriesForWorkspace(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId
    ) {
        return ResponseEntity.ok(workspaceService.getAllCategories(workspaceId));
    }

    @PostMapping("/{workspaceId}/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody CategoryRequest categoryRequest) throws AccessDeniedException {

        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService
                        .createCategory(
                                userId,
                                workspaceId,
                                categoryRequest
                        ));

    }

    @PutMapping("/{workspaceId}/categories/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("categoryId") UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest categoryRequest) throws AccessDeniedException {

        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                workspaceService.updateCategory(
                        userId,
                        workspaceId,
                        categoryId,
                        categoryRequest
                )
        );

    }

    @DeleteMapping("/{workspaceId}/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("categoryId") UUID categoryId) throws AccessDeniedException {

        UUID userId = UUID.fromString(jwt.getSubject());

        workspaceService.deleteCategory(userId, workspaceId, categoryId);

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/{workspaceId}/transactions")
    public ResponseEntity<PageResponse<TransactionResponse>>
    getTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) throws AccessDeniedException {
        UUID userId =
                UUID.fromString(jwt.getSubject());

        PageResponse<TransactionResponse> response =
                workspaceService.getTransactions(
                        userId,
                        workspaceId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workspaceId}/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody TransactionRequest transactionRequest) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                workspaceService.createTransaction(
                        userId,
                        workspaceId,
                        transactionRequest
                )
        );
    }

    @PutMapping("/{workspaceId}/transactions/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("transactionId") UUID transactionId,
            @Valid @RequestBody UpdateTransactionRequest transactionRequest) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                workspaceService.updateTransaction(
                        userId,
                        workspaceId,
                        transactionId,
                        transactionRequest
                )
        );
    }

    @DeleteMapping("/{workspaceId}/transactions/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("transactionId") UUID transactionId) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        workspaceService.deleteTransaction(userId, workspaceId, transactionId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{workspaceId}/transfers")
    public ResponseEntity<TransferResponse> createTransfer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody TransferRequest transferRequest) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                workspaceService.createTransfer(userId, workspaceId, transferRequest)
        );
    }

    @GetMapping("/{workspaceId}/transfers")
    public ResponseEntity<PageResponse<TransferResponse>> getTransfers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                workspaceService.getTransfers(userId, workspaceId, page, size)
        );
    }

    @PostMapping("/{workspaceId}/budgets")
    public ResponseEntity<BudgetItemResponse> createBudget(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody BudgetRequest budgetRequest) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                workspaceService.createBudget(userId, workspaceId, budgetRequest)
        );
    }

    @GetMapping("/{workspaceId}/budgets")
    public ResponseEntity<BudgetOverviewResponse> getBudgets(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @RequestParam(required = false) String period
    ) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                workspaceService.getBudgetOverview(userId, workspaceId, period)
        );
    }

    @PutMapping("/{workspaceId}/budgets/{budgetId}")
    public ResponseEntity<BudgetItemResponse> updateBudget(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("budgetId") UUID budgetId,
            @Valid @RequestBody UpdateBudgetRequest budgetRequest) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(
                workspaceService.updateBudget(userId, workspaceId, budgetId, budgetRequest)
        );
    }

    @DeleteMapping("/{workspaceId}/budgets/{budgetId}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("budgetId") UUID budgetId) throws AccessDeniedException {
        UUID userId = UUID.fromString(jwt.getSubject());

        workspaceService.deleteBudget(userId, workspaceId, budgetId);

        return ResponseEntity.noContent().build();
    }
}
