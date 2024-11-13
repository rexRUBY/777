package org.example.api.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(String orderBody) {
        kafkaTemplate.send("ORDER", orderBody);
//        if(orderType.equals("buy")) {
//            kafkaTemplate.send("BUY_ORDER", orderBody);
//        }
//
//        if(orderType.equals("sell")) {
//            kafkaTemplate.send("SELL_ORDER", orderBody);
//        }
    }
}
