package org.example.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.trade.dto.request.OrderRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(OrderRequest orderBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String message = objectMapper.writeValueAsString(orderBody);

            String partitionKey = orderBody.getSymbol() + "-" + orderBody.getPrice();

            kafkaTemplate.executeInTransaction(kafkaTemplate -> {
                kafkaTemplate.send("ORDER", partitionKey, message);
                return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAlarm(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
