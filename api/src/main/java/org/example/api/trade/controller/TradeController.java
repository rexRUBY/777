package org.example.api.trade.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.trade.service.TradeService;
import org.example.common.common.dto.AuthUser;
import org.example.common.trade.dto.request.TradeRequestDto;
import org.example.common.trade.dto.response.TradeResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cryptos")
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/{cryptoId}/trades")
    public ResponseEntity<TradeResponseDto> postTrade(@AuthenticationPrincipal AuthUser authUser,
                                                      @PathVariable long cryptoId,
                                                      @RequestBody TradeRequestDto tradeRequestDto){
        return ResponseEntity.ok(tradeService.postTrade(authUser,cryptoId,tradeRequestDto));
    }

    @PostMapping("/{cryptoId}/trades/subscriptions/{subscritionsId}")
    public ResponseEntity<TradeResponseDto> postSubscriptionsTrade(@AuthenticationPrincipal AuthUser authUser,
                                                                                      @PathVariable long cryptoId,
                                                                                      @PathVariable long subscritionsId,
                                                                                      @RequestBody TradeRequestDto tradeRequestDto){
        return ResponseEntity.ok(tradeService.postSubscriptionsTrade(authUser,cryptoId,subscritionsId,tradeRequestDto));
    }



    @GetMapping("/{cryptoId}/trades")
    public ResponseEntity<List<TradeResponseDto>> getTradeList(@AuthenticationPrincipal AuthUser authUser,
                                                                                  @PathVariable long cryptoId){
        return ResponseEntity.ok(tradeService.getTradeList(authUser,cryptoId));
    }

    @GetMapping("/trades")
    public ResponseEntity<List<TradeResponseDto>> getAllTradeList(@AuthenticationPrincipal AuthUser authUser){
        return ResponseEntity.ok(tradeService.getAllTradeList(authUser));
    }
}
