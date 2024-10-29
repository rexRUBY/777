package org.example.api.crypto.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.crypto.service.CryptoService;
import org.example.common.crypto.dto.CryptoListResponse;
import org.example.common.crypto.dto.CryptoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CryptoController {

    private final CryptoService cryptoService;

    @GetMapping("/crypto/{cryptoId}")
    public ResponseEntity<CryptoResponse> getCryptoInfo(@PathVariable Long cryptoId) {
        return ResponseEntity.ok(this.cryptoService.getCryptoInfo(cryptoId));
    }

    @GetMapping("/cryptos/list")
    public  ResponseEntity<CryptoListResponse> getCryptoSymbolList() {
        return ResponseEntity.ok(this.cryptoService.getCryptoSymbolList());
    }
}
