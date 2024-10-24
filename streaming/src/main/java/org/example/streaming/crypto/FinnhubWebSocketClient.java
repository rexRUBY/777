package org.example.streaming.crypto;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import org.example.streaming.crypto.dto.CryptoDataDto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@ClientEndpoint
public class FinnhubWebSocketClient {

    private Session session;

    @Value("${crypto.client.secret}")
    private String cryptoClientSecret;

    @Autowired
    private WebSocketController webSocketController;

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

        String[] symbolList = {"BINANCE:BTCUSDT", "BINANCE:ETHUSDT"};

        for (String symbol : symbolList) {
            session.getAsyncRemote().sendText(
                    String.format("{\"type\":\"subscribe\",\"symbol\":\"%s\"}", symbol)
            );
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {

            JSONObject jsonObject = new JSONObject(message);

            if (jsonObject.has("data")) {
                JSONArray dataArray = jsonObject.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);
                    String s = item.getString("s");
                    double p = item.getDouble("p");

                    s = s.split(":")[1];

                    String formattedPrice = String.format("%.5f", p);

                    CryptoDataDto cryptoDataDto = new CryptoDataDto(s, formattedPrice);

                    String json = new Gson().toJson(cryptoDataDto);

                    System.out.println(json);
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

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }
}
