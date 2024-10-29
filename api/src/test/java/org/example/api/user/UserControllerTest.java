package org.example.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.user.controller.UserController;
import org.example.api.user.service.UserService;
import org.example.common.common.dto.AuthUser;
import org.example.common.user.dto.request.UserChangePasswordRequest;
import org.example.common.user.dto.request.UserWithdrawRequest;
import org.example.common.user.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void 유저_조회_성공() throws Exception {
        // given
        long userId = 1L;
        UserResponse responseDto = new UserResponse(userId, "test@example.com", "testUser");

        when(userService.getUser(userId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("testUser"));
    }

    @Test
    public void 비밀번호_변경_성공() throws Exception {
        // given
        long userId = 1L;
        UserChangePasswordRequest passwordRequest = new UserChangePasswordRequest("oldPassword", "NewPassword1");

        doNothing().when(userService).changePassword(any(AuthUser.class), any(UserChangePasswordRequest.class));

        // when & then
        mockMvc.perform(patch("/api/v1/users", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(passwordRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void 유저_탈퇴_성공() throws Exception {
        // given
        long userId = 1L;
        UserWithdrawRequest withdrawRequest = new UserWithdrawRequest("password123");

        doNothing().when(userService).withdrawUser(any(AuthUser.class), any(UserWithdrawRequest.class));

        // when & then
        mockMvc.perform(delete("/api/v1/users", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk());
    }
}
