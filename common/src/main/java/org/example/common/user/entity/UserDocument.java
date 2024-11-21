package org.example.common.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document(collection = "users")
public class UserDocument {

    @Id
    private String id; // Long 타입 ID를 사용

    private String email;
    private String password;
    private String name;

    private boolean userStatus;

    public UserDocument(String id, String email, String password, String name, boolean userStatus) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.userStatus = userStatus;
    }

    public static UserDocument from(User user) {
        return new UserDocument(user.getId().toString(), user.getEmail(), user.getPassword(), user.getName(), user.isUserStatus());
    }

    public User toUser() {
        return new User(
                Long.valueOf(this.id), // String ID를 Long으로 변환
                this.email,
                this.password,
                this.name,
                this.userStatus
        );
    }

    public void updateUser(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}