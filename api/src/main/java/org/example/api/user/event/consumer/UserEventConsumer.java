package org.example.api.user.event.consumer;

import org.example.api.user.event.model.UserEvent;
import org.example.common.user.entity.UserDocument;
import org.example.common.user.mongo.UserQueryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private final UserQueryRepository userQueryRepository;

    public UserEventConsumer(UserQueryRepository userQueryRepository) {
        this.userQueryRepository = userQueryRepository;
    }

    @KafkaListener(
            topics = "user-events",
            groupId = "user-group",
            containerFactory = "userEventKafkaListenerContainerFactory" // 이름 일치
    )
    public void consume(UserEvent event) {
        if ("UserCreated".equals(event.getEventType())) {
            UserDocument userDocument = new UserDocument(
                    null, // MongoDB에서 ID 자동 생성
                    event.getEmail(),
                    event.getPassword(),
                    event.getName(),
                    true
            );
            userQueryRepository.save(userDocument);
        } else if ("UserUpdated".equals(event.getEventType())) {
            UserDocument userDocument = userQueryRepository.findById(event.getId().toString())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + event.getId()));
            userDocument.updateUser(event.getEmail(), event.getPassword(), event.getName());
            userQueryRepository.save(userDocument);
        }
    }
}