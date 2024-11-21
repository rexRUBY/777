package org.example.streaming.crypto.controller;

import lombok.RequiredArgsConstructor;
import org.example.streaming.crypto.service.CryptoService;
import org.example.streaming.crypto.service.OrderBookService;
import org.json.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final OrderBookService orderBookService;
    private final CryptoService cryptoService;

    public void sendToClient(String json) {
        JSONObject jsonObject = new JSONObject(json);

        String symbol = jsonObject.getString("symbol");
        String price = jsonObject.getString("price");

        messagingTemplate.convertAndSend("/topic/get_ticker_price/" + symbol, price);
    }

    @MessageMapping("/topic/get_ticker_price/{symbol}")
    @SendTo("/topic/get_ticker_price")
    public void handleSubscription(@DestinationVariable String symbol) {
        sendCryptoPrice(symbol);
    }

    @MessageMapping("/send/{counter}/{sessionId}")
    public void sendMessage(@DestinationVariable String counter, @DestinationVariable String sessionId, @Payload String message) {
        // 메시지 전송
        String destination = "/ws/" + counter + "/" + sessionId + "/websocket"; // 클라이언트가 구독하는 경로
        messagingTemplate.convertAndSend(destination, message);
        System.out.println("Sent message: " + message);
    }

    public void sendCryptoPrice(String symbol) {
        String price = cryptoService.getCryptoPrice(symbol);
        messagingTemplate.convertAndSend("/topic/get_ticker_price/" + symbol, price);
    }

    @Scheduled(fixedRate = 1000)
    public void sendOrderBookData() {
        Map<String, List<List<Object>>> data = orderBookService.getOrderBookData("LPJ_");

        messagingTemplate.convertAndSend("/topic/order_book", data);
    }
}
