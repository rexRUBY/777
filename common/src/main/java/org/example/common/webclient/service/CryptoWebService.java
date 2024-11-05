package org.example.common.webclient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.webclient.dto.CryptoWebResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoWebService {

    private final WebClient webClient;

    public Long getCryptoValueAsLong(String coin, String date, String time) {
        String symbol = coin.replace("USDT", "");
        String url = "http://43.202.60.145/get-crypto-value?coin=" + symbol + "&date=" + date + "&time=" + time;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(CryptoWebResponse.class) // 응답을 CryptoWebResponse로 변환
                .map(response -> {
                    try {
                        // 문자열을 Double로 변환한 후 Long으로 변환
                        return (long) Double.parseDouble(response.getValue());
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Invalid number format: " + response.getValue());
                    }
                })
                .block(); // 블로킹 호출
    }
}