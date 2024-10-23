package org.example.common.common.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthUser {

    private final Long id;
    private final String email;

    public static AuthUser from(Long id, String email) {
        return new AuthUser(id, email);
    }
}