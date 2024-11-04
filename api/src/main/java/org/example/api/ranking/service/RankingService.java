package org.example.api.ranking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ranking.dto.RankingListResponseDto;
import org.example.common.ranking.dto.RankingResponseDto;
import org.example.common.ranking.dto.TopRankingListResponseDto;
import org.example.common.ranking.entity.Ranking;
import org.example.common.ranking.repository.RankingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    @Transactional
    public RankingListResponseDto getRanking(String cryptoSymbol, int page, int size, LocalDateTime startDate, LocalDateTime endDate) {
        log.info(cryptoSymbol, page, size, startDate, endDate);
        Pageable pageable = PageRequest.of(page, size);
        Page<Ranking> rankings = rankingRepository.findAllByCryptoSymbolAndCreatedAtBetweenOrderByUserRankAsc(cryptoSymbol, startDate, endDate, pageable);

        List<RankingResponseDto> rankingList = rankings.stream()
                .map(ranking -> new RankingResponseDto(
                        ranking.getUserRank(),
                        ranking.getUserEmail(),
                        ranking.getRanked(),
                        ranking.getCryptoSymbol(),
                        ranking.getYield()
                ))
                .collect(Collectors.toList());

        return new RankingListResponseDto(rankingList, rankings.getTotalElements(), rankings.getTotalPages());
    }

    public TopRankingListResponseDto getTopRanking(int limit, String symbol) {
        return new TopRankingListResponseDto(rankingRepository.findTopRanking(limit, symbol));
    }
}
