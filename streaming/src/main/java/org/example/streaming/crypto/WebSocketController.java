package org.example.streaming.crypto;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate template;

    public WebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    // Finnhub WebSocket에서 메시지를 받으면 프론트로 전달
    public void sendToClient(String message) {
        template.convertAndSend("/topic/price", message); // 프론트엔드가 /topic/price를 구독해야함
    }
}
