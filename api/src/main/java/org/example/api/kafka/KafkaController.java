package org.example.api.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/test")
    public void testKafka(@RequestBody String order) {
        this.kafkaProducerService.sendOrder("order-topic", order);
    }
}
