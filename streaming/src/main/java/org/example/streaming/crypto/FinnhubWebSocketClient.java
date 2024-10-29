package org.example.streaming.crypto;

import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import org.example.streaming.crypto.dto.CryptoDataDto;
import org.example.streaming.crypto.service.CryptoService;
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

    @Autowired
    private CryptoService cryptoService;

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

        convertDataTypeAndStream(jsonObject);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    private void convertDataTypeAndStream(JSONObject data) {
        try {
            if (data.has("data")) {
                JSONArray dataArray = data.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject item = dataArray.getJSONObject(i);
                    String symbol = item.getString("s");
                    double p = item.getDouble("p");

                    symbol = symbol.split(":")[1];

                    String price = String.format("%.5f", p);

                    CryptoDataDto cryptoDataDto = new CryptoDataDto(symbol, price);

                    String json = new Gson().toJson(cryptoDataDto);

                    cryptoService.saveCryptoData(symbol, price);

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
