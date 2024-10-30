package org.example.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.auth.controller.AuthController;
import org.example.api.auth.service.AuthService;
import org.example.common.auth.dto.request.ResetPasswordRequest;
import org.example.common.auth.dto.request.SigninRequest;
import org.example.common.auth.dto.request.SignupRequest;
import org.example.common.auth.dto.response.SigninResponse;
import org.example.common.auth.dto.response.SignupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setValidator(new LocalValidatorFactoryBean())
                .alwaysDo(print()) // 요청/응답 로그 출력
                .build();
    }

    @Test
    public void 회원가입_성공() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "Valid123@", "testUser");
        SignupResponse signupResponse = new SignupResponse("generatedToken");

        when(authService.signup(any(SignupRequest.class))).thenReturn(signupResponse);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("generatedToken"));
    }

    @Test
    public void 로그인_성공() throws Exception {
        // given
        SigninRequest signinRequest = new SigninRequest("test@example.com", "Valid123@Password");
        SigninResponse signinResponse = new SigninResponse("generatedToken");

        when(authService.signin(any(SigninRequest.class))).thenReturn(signinResponse);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(signinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("generatedToken"));
    }

    @Test
    public void 비밀번호_초기화_성공() throws Exception {
        // given
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("test@example.com", "resetToken", "NewValid123@");

        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        // when & then
        mockMvc.perform(patch("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk());
    }
}
