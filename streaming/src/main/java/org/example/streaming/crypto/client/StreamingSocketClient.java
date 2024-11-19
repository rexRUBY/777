package org.example.streaming.crypto.client;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.*;
import lombok.RequiredArgsConstructor;
import org.example.streaming.crypto.controller.WebSocketController;
import org.example.streaming.crypto.dto.CryptoDataDto;
import org.example.streaming.crypto.service.CryptoService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
@ClientEndpoint
@RequiredArgsConstructor
public class StreamingSocketClient {

    private Session session;

    @Value("${crypto.client.secret}")
    private String cryptoClientSecret;
    private final WebSocketController webSocketController;
    private final CryptoService cryptoService;
    private final ConcurrentHashMap<String, String> latestPriceData = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Map<String, String> batchedData = new ConcurrentHashMap<>();

    @PostConstruct
    public void startWebSocket() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            String uri = "wss://ws.finnhub.io?token=" + cryptoClientSecret;
            container.connectToServer(this, new URI(uri));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retryConnect(int retries) {
        if (retries > 0) {
            try {
                Thread.sleep(5000);  // 5초 대기 후 재시도
                startWebSocket();  // 재시도
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Failed to connect to WebSocket after multiple attempts.");
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to Finnhub WebSocket");

        String[] symbolList = this.cryptoService.getCryptoSymbolList().toArray(new String[0]);

        for (String symbol : symbolList) {
            String prefixedSymbol = "BINANCE:" + symbol;
            session.getAsyncRemote().sendText(
                    String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", prefixedSymbol)
            );
        }
    }

    @OnMessage
    public void onMessage(String message) {
        JSONObject jsonObject = new JSONObject(message);
        convertDataTypeAndStoreLocally(jsonObject);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
        scheduler.shutdown();
    }

    @PreDestroy
    public void cleanUp() {
        scheduler.shutdown();
    }

    private void convertDataTypeAndStoreLocally(JSONObject data) {
        executorService.submit(() -> {
            try {
                if (data.has("data")) {
                    JSONArray dataArray = data.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);
                        String symbol = item.getString("s").split(":")[1];
                        String formattedPrice = String.format("%.5f", item.getDouble("p"));
                        cryptoService.saveCurrentCryptoPrice(symbol, formattedPrice);
                        batchedData.put(symbol, formattedPrice);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Scheduled(fixedRate = 1000)
    public void sendBatchedData() {
        for (Map.Entry<String, String> entry : batchedData.entrySet()) {
            CryptoDataDto dto = new CryptoDataDto(entry.getKey(), entry.getValue());
            String json = new Gson().toJson(dto);
            webSocketController.sendToClient(json);
        }
        batchedData.clear();
    }
}