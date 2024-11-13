package org.example.alarm.controller;

import lombok.RequiredArgsConstructor;
import org.example.alarm.service.KafkaSseService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class KafkaSseController {

    private final KafkaSseService kafkaSseService;

    @GetMapping(
            value = "/topics/{topic}/subscribe",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<String>> streamMessages(@PathVariable String topic) {
        return kafkaSseService.streamMessages(topic);
    }
}
