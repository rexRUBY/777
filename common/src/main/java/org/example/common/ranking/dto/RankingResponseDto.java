package org.example.common.ranking.dto;

import lombok.Getter;
import org.example.common.ranking.entity.Ranked;

@Getter
public class RankingResponseDto {
    private Long userRank;

    private String userEmail;

    private Ranked ranked;

    private String cryptoSymbol;

    private Double yield;

    public RankingResponseDto(Long userRank, String userEmail, Ranked ranked, String cryptoSymbol, Double yield) {
        this.userRank = userRank;
        this.userEmail = userEmail;
        this.ranked = ranked;
        this.cryptoSymbol = cryptoSymbol;
        this.yield = yield;
    }
}
