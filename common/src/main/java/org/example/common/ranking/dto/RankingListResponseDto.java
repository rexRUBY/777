package org.example.common.ranking.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class RankingListResponseDto {
    private List<RankingResponseDto> rankingList;
    private long totalElements;
    private int totalPages;

    public RankingListResponseDto(List<RankingResponseDto> rankingList, long totalElements, int totalPages) {
        this.rankingList = rankingList;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
