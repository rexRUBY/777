package org.example.streaming.crypto;

import org.example.streaming.crypto.service.CryptoService;
import org.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    private final CryptoService cryptoService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, CryptoService cryptoService) {
        this.messagingTemplate = messagingTemplate;
        this.cryptoService = cryptoService;
    }

    public void sendToClient(String json) {
        JSONObject jsonObject = new JSONObject(json);
        String symbol = jsonObject.getString("symbol");
        String price = cryptoService.getCryptoPrice(symbol);

        messagingTemplate.convertAndSend("/topic/get_ticker_price/" + symbol, price);
    }

}
