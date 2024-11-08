package org.example.alarm.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaAlarmConsumerService {

    private final StringRedisTemplate redisTemplate;
    @KafkaListener(topics = "alarm-topic", groupId = "alarm-group")
    public void consumeOrder(String message) {
        System.out.println("alarm received: " + message);
        // 여기에 메시지 처리 로직을 추가

        redisTemplate.opsForList().leftPush("alarmTestData", message);
    }
}
