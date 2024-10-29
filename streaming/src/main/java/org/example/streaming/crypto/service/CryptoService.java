package org.example.streaming.crypto.service;

import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CryptoService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private final CryptoRepository cryptoRepository;

    public CryptoService(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    public void saveCryptoData(String symbol, String price) {
        redisTemplate.opsForHash().put("STREAM_ASSET_PRICE", symbol, price);
    }

    public String getCryptoPrice(String symbol) {
        return redisTemplate.opsForHash().get("STREAM_ASSET_PRICE", symbol).toString();
    }

    public List<String> getCryptoSymbolList() {
        List<Crypto> cryptoList = this.cryptoRepository.findAll();

        return cryptoList.stream()
                .map(Crypto::getSymbol)
                .collect(Collectors.toList());
    }
}
