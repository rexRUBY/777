package org.example.api.subscriptions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.subscriptions.service.SubscriptionsService;
import org.example.common.auth.dto.request.UnFollowResponse;
import org.example.common.common.dto.AuthUser;
import org.example.common.subscriptions.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionsControllerTest {
    @Mock
    private SubscriptionsService subscriptionsService;
    @InjectMocks
    private SubscriptionsController subscriptionsController;
    private MockMvc mvc;

    private AuthUser testAuthUser;
    private FollowingRequest testFollowingRequest;
    private FollowingResponse testFollowingResponse;
    private FollowerResponse testFollowerResponse;
    private FollowingListResponse testFollowingListResponse;
    private FollowerListResponse testFollowerListResponse;
    private UnFollowResponse testUnFollowResponse;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(subscriptionsController).build();
        testAuthUser = AuthUser.from(1L, "test1@email.com");
        testFollowingRequest = new FollowingRequest();
        ReflectionTestUtils.setField(testFollowingRequest, "followingUserId", 2L);
        ReflectionTestUtils.setField(testFollowingRequest, "cryptoId", 1L);
        ReflectionTestUtils.setField(testFollowingRequest, "cryptoAmount", 1.0);
        testFollowingResponse = new FollowingResponse("test2", "BTC");
        testFollowerResponse = new FollowerResponse("test2", "BTC");
        testFollowingListResponse = new FollowingListResponse(List.of(testFollowingResponse));
        testFollowerListResponse = new FollowerListResponse(List.of(testFollowerResponse));
        testUnFollowResponse = new UnFollowResponse("test1@email.com", "BTC", 1.0, 300L);
    }

    @Test
    @DisplayName("구독 생성")
    void subscribe() throws Exception {
        // given
        given(subscriptionsService.subscribe(any(), any())).willReturn(testFollowingResponse);
        // when - then
        mvc.perform(post("/api/v1/subscriptions")
                        .content(objectMapper.writeValueAsString(testFollowingRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("구독 생성 DB에 저장 성공")
    void getFollowing() throws Exception {
        // given
        given(subscriptionsService.getFollowing(any())).willReturn(testFollowingListResponse);
        // when - then
        mvc.perform(get("/api/v1/subscriptions/following"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("구독 생성 DB에 저장 성공")
    void getFollower() throws Exception {
        // given
        given(subscriptionsService.getFollower(any())).willReturn(testFollowerListResponse);
        // when - then
        mvc.perform(get("/api/v1/subscriptions/follower"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("구독 생성 DB에 저장 성공")
    void unFollowing() throws Exception {
        // given
        given(subscriptionsService.unFollowing(any(), anyLong())).willReturn(testUnFollowResponse);
        // when - then
        mvc.perform(delete("/api/v1/subscriptions/{subscriptionsId}", 1L))
                .andExpect(status().isOk());
    }
}