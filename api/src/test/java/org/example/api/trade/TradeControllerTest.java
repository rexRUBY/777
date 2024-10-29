package org.example.api.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.trade.controller.TradeController;
import org.example.api.trade.service.TradeService;
import org.example.common.trade.dto.request.TradeRequestDto;
import org.example.common.trade.dto.response.TradeResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TradeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TradeService tradeService;

    @InjectMocks
    private TradeController tradeController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(tradeController).build();
    }

    @Test
    public void 거래_등록_성공() throws Exception {
        // given
        long cryptoId = 1L;
        TradeRequestDto tradeRequestDto = new TradeRequestDto();
        ReflectionTestUtils.setField(tradeRequestDto, "amount", 10.0);
        ReflectionTestUtils.setField(tradeRequestDto, "tradeType", "BUY");
        ReflectionTestUtils.setField(tradeRequestDto, "tradeFor", "SELF");

        TradeResponseDto responseDto = new TradeResponseDto("BTC", 10.0, "BUY", 5000L);
        when(tradeService.postTrade(any(), eq(cryptoId), any(TradeRequestDto.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/cryptos/{cryptoId}/trades", cryptoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tradeRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cryptoSymbol").value("BTC"))
                .andExpect(jsonPath("$.amount").value(10.0))
                .andExpect(jsonPath("$.billType").value("BUY"))  // tradeType으로 변경
                .andExpect(jsonPath("$.price").value(5000L));
    }
}
