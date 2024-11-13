package org.example.api.trade.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.trade.service.TradeService;
import org.example.common.common.dto.AuthUser;
import org.example.common.trade.dto.request.TradeRequestDto;
import org.example.common.trade.dto.response.TradeListResponseDto;
import org.example.common.trade.dto.response.TradeResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cryptos")
public class TradeController {

    private final TradeService tradeService;

    @PostMapping("/{cryptoId}/trades")
    public ResponseEntity<TradeResponseDto> postTrade(@AuthenticationPrincipal AuthUser authUser,
                                                      @PathVariable long cryptoId,
                                                      @RequestBody TradeRequestDto tradeRequestDto) {
        return ResponseEntity.ok(tradeService.postTrade(authUser, cryptoId, tradeRequestDto));
    }

    @PostMapping("/{cryptoId}/trades/subscriptions/{subscritionsId}")
    public ResponseEntity<TradeResponseDto> postSubscriptionsTrade(@AuthenticationPrincipal AuthUser authUser,
                                                                   @PathVariable long cryptoId,
                                                                   @PathVariable long subscritionsId,
                                                                   @RequestBody TradeRequestDto tradeRequestDto) {
        return ResponseEntity.ok(tradeService.postSubscriptionsTrade(authUser, cryptoId, subscritionsId, tradeRequestDto));
    }

    @GetMapping("/{cryptoId}/trades")
    public ResponseEntity<List<TradeResponseDto>> getTradeList(@AuthenticationPrincipal AuthUser authUser,
                                                               @PathVariable long cryptoId) {
        return ResponseEntity.ok(tradeService.getTradeList(authUser, cryptoId));
    }

    @GetMapping("/trades")
    public ResponseEntity<List<TradeResponseDto>> getAllTradeList(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(tradeService.getAllTradeList(authUser));
    }

    @GetMapping("/trades/pagination")
    public ResponseEntity<TradeListResponseDto> getTradeListPage(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam Long cryptoId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(tradeService.getTradeListPage(authUser, cryptoId, page, size, startDate, endDate));
    }
}
