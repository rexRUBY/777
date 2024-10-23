package org.example.common.common.config;

import org.example.common.common.dto.AuthUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthUser authUser;

    public JwtAuthenticationToken(AuthUser authUser) {
        super(null); // 부모 클래스의 생성자 호출
        this.authUser = authUser;
        setAuthenticated(true); // 인증 상태를 true로 설정
    }

    @Override
    public Object getCredentials() {
        return null; // 자격 증명은 필요하지 않음
    }

    @Override
    public Object getPrincipal() {
        return authUser; // 인증된 사용자의 정보를 반환
    }
}