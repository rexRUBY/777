package org.example.streaming.crypto.service;

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
public class CryptoService {
    private final StringRedisTemplate redisTemplate;

    private final CryptoRepository cryptoRepository;

    private static final int MAX_SIZE = 900;

    public CryptoService(StringRedisTemplate redisTemplate, CryptoRepository cryptoRepository) {
        this.redisTemplate = redisTemplate;
        this.cryptoRepository = cryptoRepository;
    }

    public void saveCryptoData(String symbol, String price, String createdAt) {
        // 심볼 별 Redis Key 생성 (예: "COIN:BTCUSDT")
        String key = "COIN:" + symbol;

        // Redis 리스트의 맨 앞에 가격 데이터와 createdAt 정보 추가
        redisTemplate.opsForList().leftPush(key, price + "," + createdAt);

        // 리스트의 길이가 900을 초과할 경우, 가장 오래된 데이터 삭제
        redisTemplate.opsForList().trim(key, 0, MAX_SIZE - 1);
    }



    public String getCryptoPrice(String symbol) {
        return redisTemplate.opsForHash().get("STREAM_ASSET_PRICE", symbol).toString();
    }

    // 코인의 현재가격 STREAM_ASSET_PRICE 키에 저장
    public void saveCurrentCryptoPrice(String symbol, String price) {
        redisTemplate.opsForHash().put("STREAM_ASSET_PRICE", symbol, price);
    }

    // 현재 도메인에서 지원하는 코인 종류 가져오기
    public List<String> getCryptoSymbolList() {
        List<Crypto> cryptoList = this.cryptoRepository.findAll();

        return cryptoList.stream()
                .map(Crypto::getSymbol)
                .collect(Collectors.toList());
    }

    // 1초마다 특정 심볼의 가격을 조회하는 스케줄러
    @Scheduled(fixedRate = 1000) // 1초마다 실행
    public void fetchAndStoreCryptoPrice() {
        Set<Object> symbolObjects = redisTemplate.opsForHash().keys("STREAM_ASSET_PRICE");

        // Object를 String으로 변환하여 Set<String>으로 저장
        Set<String> symbols = symbolObjects.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        for (String symbol : symbols) {
            String price = getCryptoPrice(symbol); // 가격 조회

            // 조회한 가격 처리
            if (price != null) {
                String createdAt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'")
                        .withZone(ZoneId.of("Asia/Seoul"))
                        .format(Instant.now());

                saveCryptoData(symbol, price, createdAt); // 가격 데이터를 Redis에 저장

            } else {
                System.out.println("No price found for " + symbol);
            }
        }
    }
}
