package org.example.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.example.common.trade.dto.request.OrderRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/test")
    public String testKafka(@RequestBody OrderRequest orderBody) {
        return this.kafkaProducerService.sendOrder(orderBody);
    }

    @PostMapping("/test/alarm")
    public void testAlarmKafka(@RequestBody AlarmRequest alarm) {
        this.kafkaProducerService.sendAlarm("alarm-topic", alarm.getAlarm());
    }
}
