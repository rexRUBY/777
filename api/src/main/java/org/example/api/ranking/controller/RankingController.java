package org.example.api.ranking.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.ranking.service.RankingService;
import org.example.common.ranking.dto.RankingListResponseDto;
import org.example.common.ranking.dto.TopRankingListResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/crypto/{cryptoSymbol}/{page}/{size}")
    public ResponseEntity<RankingListResponseDto> getRanking(
            @PathVariable String cryptoSymbol,
            @PathVariable int page,
            @PathVariable int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        return ResponseEntity.ok(this.rankingService.getRanking(cryptoSymbol, page, size, startDate, endDate));
    }

    @GetMapping("/top/{limit}/{symbol}")
    public ResponseEntity<TopRankingListResponseDto> getTopRanking(@PathVariable int limit, @PathVariable String symbol) {
        return ResponseEntity.ok(this.rankingService.getTopRanking(limit, symbol));
    }
}
