package org.example.alarm.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaSseService {

    public Flux<ServerSentEvent<String>> streamMessages(String topic) {

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "alarm-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // ReceiverOptions 생성, 구독할 topic을 지정
        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(consumerProps);
        receiverOptions = receiverOptions.subscription(Collections.singletonList(topic));

        // KafkaReceiver 생성
        KafkaReceiver<String, String> receiver = KafkaReceiver.create(receiverOptions);

        // Kafka로부터 받은 메시지를 Flux<ServerSentEvent>로 변환하여 반환
        return receiver.receive()
                .map(ConsumerRecord::value)     // Kafka 메시지 본문을 추출
                .map(message -> ServerSentEvent.<String>builder()   // ServerSentEvent로 변환
                        .data(message)
                        .build());
    }
}
