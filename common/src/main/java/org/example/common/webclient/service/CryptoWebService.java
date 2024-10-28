package org.example.common.webclient.service;

import lombok.RequiredArgsConstructor;
import org.example.common.webclient.dto.CryptoWebResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class CryptoWebService {

    private final WebClient webClient;

    public Long getCryptoValueAsLong(String coin, String date, String time) {
        String url = "http://13.125.231.198:8080/get-crypto-value?coin=" + coin + "&date=" + date + "&time=" + time;

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