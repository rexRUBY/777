package org.example.api.wallet.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.wallet.service.WalletService;
import org.example.common.common.dto.AuthUser;
import org.example.common.wallet.dto.WalletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    @GetMapping()
    public ResponseEntity<List<WalletResponse>> getWallets(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(walletService.getWallets(authUser));
    }
}
