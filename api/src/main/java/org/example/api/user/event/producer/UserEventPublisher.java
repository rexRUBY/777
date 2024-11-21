package org.example.api.user.event.producer;

import lombok.RequiredArgsConstructor;
import org.example.api.user.event.model.UserEvent;
import org.example.common.user.entity.User;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void publishUserCreatedEvent(User user) {
        UserEvent event = new UserEvent(user.getId(), user.getEmail(), user.getName(), user.getPassword(), "UserCreated");
        kafkaTemplate.send("user-events", event);
    }

    public void publishUserUpdatedEvent(User user) {
        UserEvent event = new UserEvent(user.getId(), user.getEmail(), user.getName(), user.getPassword(), "UserCreated");
        kafkaTemplate.send("user-events", event);
    }
}