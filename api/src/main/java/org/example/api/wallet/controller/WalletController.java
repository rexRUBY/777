package org.example.api.wallet.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.wallet.service.WalletService;
import org.example.common.common.dto.AuthUser;
import org.example.common.wallet.dto.request.ChargeRequest;
import org.example.common.wallet.dto.response.WalletHistoryListResponseDto;
import org.example.common.wallet.dto.response.WalletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @PostMapping("/charge")
    public ResponseEntity<String> chargeCash(@AuthenticationPrincipal AuthUser authUser, @RequestBody ChargeRequest request) {
        return ResponseEntity.ok(walletService.chargeCash(authUser, request));
    }

    @GetMapping("/history/{page}/{size}")
    public ResponseEntity<WalletHistoryListResponseDto> getWalletHistoryPage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable int page,
            @PathVariable int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(walletService.getWalletHistoryPage(authUser, page, size, startDate, endDate));
    }
}
