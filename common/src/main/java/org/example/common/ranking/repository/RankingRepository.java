package org.example.common.ranking.repository;

import org.example.common.ranking.entity.Ranking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking,Long> {
    boolean existsByUserEmailAndCryptoSymbolAndCreatedAt(String userEmail, String cryptoSymbol, LocalDateTime time);
    boolean existsByUserEmailAndCryptoSymbolAndCreatedAtAndUserRankNotNull(String userEmail, String cryptoSymbol, LocalDateTime time);

    @Query("SELECT r FROM Ranking r WHERE r.cryptoSymbol = :cryptoSymbol " +
            "AND (:startDate IS NULL OR r.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR r.createdAt <= :endDate) " +
            "ORDER BY r.userRank ASC")
    Page<Ranking> findAllByCryptoSymbolAndCreatedAtBetweenOrderByUserRankAsc(
            String cryptoSymbol, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT r FROM Ranking r WHERE r.cryptoSymbol = :symbol AND r.userRank BETWEEN 1 AND :limit ORDER BY r.userRank ASC")
    List<Ranking> findTopRanking(int limit, String symbol);
}
