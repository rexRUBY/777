package org.example.api.crypto.service;

import lombok.RequiredArgsConstructor;
import org.example.common.crypto.dto.CryptoLatestPriceResponseDto;
import org.example.common.crypto.dto.CryptoListResponse;
import org.example.common.crypto.dto.CryptoResponse;
import org.example.common.crypto.entity.Crypto;
import org.example.common.crypto.repository.CryptoRepository;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CryptoService {

    private final CryptoRepository cryptoRepository;

    private final RedisTemplate<String, String> redisTemplate;

    public CryptoResponse getCryptoInfo(Long cryptoId) {
        Crypto crypto = cryptoRepository.findById(cryptoId).orElseThrow(()->new NullPointerException("No such coin"));
        return new CryptoResponse(crypto);
    }

    public CryptoListResponse getCryptoSymbolList() {
        List<Crypto> cryptoList = cryptoRepository.findAll();

        return new CryptoListResponse(cryptoList);
    }
    public CryptoLatestPriceResponseDto getLatestCryptoPrice(String symbol, String limit) {
        String key = "COIN:" + symbol; // Redis 키 생성
        ListOperations<String, String> listOps = redisTemplate.opsForList();

        int limitValue;

        // limit이 문자열일 경우 정수로 변환
        try {
            limitValue = Integer.parseInt(limit);
        } catch (NumberFormatException e) {
            limitValue = 900; // 기본값 설정
        }

        // Redis에서 가격 리스트 가져오기
        List<String> prices = listOps.range(key, 0, limitValue - 1); // limit - 1까지 조회

        // 조회한 가격 데이터가 없다면 빈 리스트 반환
        if (prices == null || prices.isEmpty()) {
            return new CryptoLatestPriceResponseDto(symbol, new ArrayList<>()); // 빈 가격 리스트
        }

        Collections.reverse(prices);

        // 최신 가격 데이터 반환
        return new CryptoLatestPriceResponseDto(symbol, prices);
    }

}
