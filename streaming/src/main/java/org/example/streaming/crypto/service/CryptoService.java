package org.example.streaming.crypto.service;

import lombok.RequiredArgsConstructor;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CryptoService {
    private final StringRedisTemplate redisTemplate;

    private final CryptoRepository cryptoRepository;

    private static final int MAX_SIZE = 900;

    public void saveCryptoData(String symbol, String price, String createdAt) {
        String key = "COIN:" + symbol;

        redisTemplate.opsForList().leftPush(key, price + "," + createdAt);

        redisTemplate.opsForList().trim(key, 0, MAX_SIZE - 1);
    }

    public String getCryptoPrice(String symbol) {
        return redisTemplate.opsForHash().get("STREAM_ASSET_PRICE", symbol).toString();
    }

    public void saveCurrentCryptoPrice(String symbol, String price) {
        redisTemplate.opsForHash().put("STREAM_ASSET_PRICE", symbol, price);
    }

    public List<String> getCryptoSymbolList() {
        List<Crypto> cryptoList = this.cryptoRepository.findAll();

        return cryptoList.stream()
                .map(Crypto::getSymbol)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 1000)
    public void fetchAndStoreCryptoPrice() {
        Set<Object> symbolObjects = redisTemplate.opsForHash().keys("STREAM_ASSET_PRICE");

        Set<String> symbols = symbolObjects.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        for (String symbol : symbols) {
            String price = getCryptoPrice(symbol);

            if (price != null) {
                String createdAt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'")
                        .withZone(ZoneId.of("Asia/Seoul"))
                        .format(Instant.now());

                saveCryptoData(symbol, price, createdAt);

            } else {
                System.out.println("No price found for " + symbol);
            }
        }
    }
}
