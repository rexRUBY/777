package org.example.common.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.common.user.entity.User;

@Getter
@RequiredArgsConstructor
public class UserResponse {

    private final Long userId;
    private final String email;
    private final String name;

    public static UserResponse entityToDto(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }
}
