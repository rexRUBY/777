package org.example.api.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KafkaController {
    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/test")
    public void testKafka(@RequestBody String orderBody) {
        this.kafkaProducerService.sendOrder(orderBody);
    }
    @PostMapping("/test/alarm")
    public void testAlarmKafka(@RequestBody AlarmRequest alarm) {
        this.kafkaProducerService.sendAlarm("alarm-topic", alarm.getAlarm());
    }
}
