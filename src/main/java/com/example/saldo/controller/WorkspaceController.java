package com.example.saldo.controller;

import com.example.saldo.dto.workspace.*;
import com.example.saldo.service.WorkspaceService;
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
            @RequestBody WalletRequest walletRequest) {
        try {
            UUID userId = UUID.fromString(jwt.getSubject());
            WalletResponse wallet = workspaceService.createWallet(userId, workspaceId, walletRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{workspaceId}/categories")
    public ResponseEntity<List<CategoryResponse>> getCategoriesForWorkspace(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId
    ){
        return ResponseEntity.ok(workspaceService.getAllCategories(workspaceId));
    }

    @GetMapping("/{workspaceId}/transactions")
    public ResponseEntity<PageResponse<TransactionResponse>>
    getTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        try {


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
        }catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
