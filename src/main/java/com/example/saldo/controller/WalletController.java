package com.example.saldo.controller;

import com.example.saldo.dto.workspace.OwnedWalletResponse;
import com.example.saldo.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<List<OwnedWalletResponse>> getMyWallets(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(workspaceService.getMyWallets(userId));
    }
}
