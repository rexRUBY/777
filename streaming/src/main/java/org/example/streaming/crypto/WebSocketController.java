package org.example.streaming.crypto;

import org.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;

    }

    public void sendToClient(String json) {
        JSONObject jsonObject = new JSONObject(json);

        String symbol = jsonObject.getString("symbol");
        String price = jsonObject.getString("price");

        messagingTemplate.convertAndSend("/topic/get_ticker_price/" + symbol, price);
    }

}
