package org.example.common.ranking.dto;

import lombok.Getter;
import org.example.common.ranking.entity.Ranking;

import java.util.List;

@Getter
public class TopRankingListResponseDto {
    private List<Ranking> rankingList;

    public TopRankingListResponseDto(List<Ranking> topRanking) {
        this.rankingList = topRanking;
    }
}
