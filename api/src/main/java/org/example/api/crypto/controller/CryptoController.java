package org.example.api.crypto.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.crypto.service.CryptoService;
import org.example.common.crypto.dto.CryptoLatestPriceResponseDto;
import org.example.common.crypto.dto.CryptoListResponse;
import org.example.common.crypto.dto.CryptoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CryptoController {

    private final CryptoService cryptoService;

    @GetMapping("/cryptos/{cryptoId}")
    public ResponseEntity<CryptoResponse> getCryptoInfo(@PathVariable Long cryptoId) {
        return ResponseEntity.ok(this.cryptoService.getCryptoInfo(cryptoId));
    }

    @GetMapping("/cryptos/list")
    public  ResponseEntity<CryptoListResponse> getCryptoSymbolList() {
        return ResponseEntity.ok(this.cryptoService.getCryptoSymbolList());
    }

    @GetMapping("/cryptos/latest/price")
    public ResponseEntity<CryptoLatestPriceResponseDto> getLatestCryptoPrice(
            @RequestParam String symbol,
            @RequestParam String limit
    ) {
        return ResponseEntity.ok(this.cryptoService.getLatestCryptoPrice(symbol, limit));
    }
}
