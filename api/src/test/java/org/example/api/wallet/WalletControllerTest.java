//package org.example.api.wallet;
//
//import org.example.api.wallet.controller.WalletController;
//import org.example.api.wallet.service.WalletService;
//import org.example.common.common.dto.AuthUser;
//import org.example.common.wallet.dto.response.WalletResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//import static org.mockito.ArgumentMatchers.any; // Mockito의 any()를 명확하게 지정
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.hamcrest.Matchers.*;
//
//@ExtendWith(MockitoExtension.class)
//public class WalletControllerTest {
//
//    @Mock
//    private WalletService walletService;
//
//    @InjectMocks
//    private WalletController walletController;
//
//    private MockMvc mockMvc;
//
//    @BeforeEach
//    void setUp() {
//        mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
//    }
//
//    @Test
//    @WithMockUser // 인증된 사용자
//    public void 지갑_조회_성공() throws Exception {
//        // given
//        AuthUser authUser = new AuthUser(1L, "test@example.com");
//        List<WalletResponse> walletResponses = Arrays.asList(
////                new WalletResponse(1L, 10.0, "BTC", "test@example.com"),
////                new WalletResponse(2L, 5.0, "ETH", "test@example.com")
//        );
//
//        when(walletService.getWallets(any(AuthUser.class))).thenReturn(walletResponses);
//
//        // when & then
//        mockMvc.perform(get("/api/v1/wallets")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].cryptoSymbol", is("BTC")))
//                .andExpect(jsonPath("$[0].amount", is(10.0)))
//                .andExpect(jsonPath("$[1].cryptoSymbol", is("ETH")))
//                .andExpect(jsonPath("$[1].amount", is(5.0)));
//
//        verify(walletService, times(1)).getWallets(any(AuthUser.class));
//    }
//}
