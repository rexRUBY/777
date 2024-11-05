package org.example.streaming.crypto;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.*;
import org.example.streaming.crypto.dto.CryptoDataDto;
import org.example.streaming.crypto.service.CryptoService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
@ClientEndpoint
public class StreamingSocketClient {

    private Session session;

    @Value("${crypto.client.secret}")
    private String cryptoClientSecret;
    private final WebSocketController webSocketController;

    private final CryptoService cryptoService;

    public StreamingSocketClient(
            WebSocketController webSocketController,
            CryptoService cryptoService

    ) {
        this.webSocketController = webSocketController;
        this.cryptoService = cryptoService;
    }

    // 메모리에 저장할 마지막 가격 정보
    private final ConcurrentHashMap<String, String> latestPriceData = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
        try {
            if (data.has("data")) {
                JSONArray dataArray = data.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);
                    String symbol = item.getString("s");
                    double p = item.getDouble("p");

                    symbol = symbol.split(":")[1];

                    String price = String.format("%.5f", p);

                    // 메모리에 최신 데이터 저장
                    latestPriceData.put(symbol, price);
                    cryptoService.saveCurrentCryptoPrice(symbol, price);
                    // 클라이언트에 실시간 데이터 전송
                    CryptoDataDto cryptoDataDto = new CryptoDataDto(symbol, price);

                    String json = new Gson().toJson(cryptoDataDto);
                    webSocketController.sendToClient(json);
                }
            } else {
                System.out.println("\"data\" key not found in JSON.");
            }
        } catch (JSONException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
