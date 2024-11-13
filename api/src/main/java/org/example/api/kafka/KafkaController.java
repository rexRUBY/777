package org.example.api.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/test")
    public void testKafka(@RequestBody String orderBody) {
        this.kafkaProducerService.sendOrder(orderBody);
    }
}
