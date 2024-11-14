package org.example.streaming.crypto.controller;

import lombok.RequiredArgsConstructor;
import org.example.streaming.crypto.service.OrderBookService;
import org.json.JSONObject;
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

    public void sendToClient(String json) {
        JSONObject jsonObject = new JSONObject(json);

        String symbol = jsonObject.getString("symbol");
        String price = jsonObject.getString("price");

        messagingTemplate.convertAndSend("/topic/get_ticker_price/" + symbol, price);
    }

    @Scheduled(fixedRate = 1000)
    public void sendOrderBookData() {
        Map<String, List<List<Object>>> data = orderBookService.getOrderBookData("LPJ_");

        messagingTemplate.convertAndSend("/topic/order_book", data);
    }

}
