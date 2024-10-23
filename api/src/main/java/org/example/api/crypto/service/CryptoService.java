package org.example.api.crypto.service;

import lombok.RequiredArgsConstructor;
import org.example.common.crypto.dto.CryptoResponse;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CryptoService {

    private final CryptoRepository cryptoRepository;

    public CryptoResponse getCryptoInfo(Long cryptoId) {
        Crypto crypto = this.cryptoRepository.findById(cryptoId).orElseThrow();
        return new CryptoResponse(crypto);
    }
}
