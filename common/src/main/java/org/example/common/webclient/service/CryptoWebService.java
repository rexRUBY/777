package org.example.common.webclient.service;

import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import lombok.extern.slf4j.Slf4j;
import org.example.common.webclient.dto.CryptoWebResponse;
=======
import org.springframework.beans.factory.annotation.Value;
>>>>>>> origin/dev
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.example.common.webclient.dto.CryptoWebResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoWebService {

    private final WebClient webClient;

    @Value("${crypto.api.base-url}")
    private String baseUrl;

    public Long getCryptoValueAsLong(String coin, String date, String time) {

        String symbol = coin.replace("USDT", "");

        String url = baseUrl + "/get-crypto-value?coin=" + symbol + "&date=" + date + "&time=" + time;


        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(CryptoWebResponse.class)
                .map(response -> {
                    try {
                        return (long) Double.parseDouble(response.getValue());
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Invalid number format: " + response.getValue());
                    }
                })
                .block();
    }
}