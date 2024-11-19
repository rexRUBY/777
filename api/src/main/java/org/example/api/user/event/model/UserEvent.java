package org.example.api.user.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {

    private Long id; // 사용자 ID
    private String email; // 사용자 이메일
    private String name; // 사용자 이름
    private String password; // 사용자 비밀번호
    private String eventType; // 이벤트 유형
    private LocalDateTime timestamp; // 이벤트 발생 시간

    // 생성 시각을 포함하는 생성자
    public UserEvent(Long id, String email, String name, String password, String eventType) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now(); // 이벤트 발생 시각을 자동으로 설정
    }
}