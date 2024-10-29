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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(jsonPath("$.billType").value("BUY"))
                .andExpect(jsonPath("$.price").value(5000L));
    }

    @Test
    public void 특정_거래_목록_조회_성공() throws Exception {
        // given
        long cryptoId = 1L;
        List<TradeResponseDto> responseDtoList = List.of(
                new TradeResponseDto("BTC", 10.0, "BUY", 5000L),
                new TradeResponseDto("ETH", 5.0, "SELL", 3000L)
        );

        when(tradeService.getTradeList(any(), eq(cryptoId))).thenReturn(responseDtoList);

        // when & then
        mockMvc.perform(get("/api/v1/cryptos/{cryptoId}/trades", cryptoId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cryptoSymbol").value("BTC"))
                .andExpect(jsonPath("$[0].amount").value(10.0))
                .andExpect(jsonPath("$[0].billType").value("BUY"))
                .andExpect(jsonPath("$[0].price").value(5000L))
                .andExpect(jsonPath("$[1].cryptoSymbol").value("ETH"))
                .andExpect(jsonPath("$[1].amount").value(5.0))
                .andExpect(jsonPath("$[1].billType").value("SELL"))
                .andExpect(jsonPath("$[1].price").value(3000L));
    }
}
