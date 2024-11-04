package org.example.common.ranking.repository;

import org.example.common.ranking.entity.Ranking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT COUNT(r) > 0 FROM Ranking r " +
            "WHERE r.userEmail = :userEmail " +
            "AND r.cryptoSymbol = :cryptoSymbol " +
            "AND r.createdAt BETWEEN :time AND :time2")
    boolean existsByUserEmailAndCryptoSymbolAndCreatedAtBetween(
            @Param("userEmail") String userEmail,
            @Param("cryptoSymbol") String cryptoSymbol,
            @Param("time") LocalDateTime time,
            @Param("time2") LocalDateTime time2);

    @Query("SELECT COUNT(r) > 0 FROM Ranking r " +
            "WHERE r.userEmail = :userEmail " +
            "AND r.cryptoSymbol = :cryptoSymbol " +
            "AND r.createdAt BETWEEN :startTime AND :endTime " +
            "AND r.userRank IS NOT NULL")
    boolean existsByUserEmailAndCryptoSymbolAndUserRankNotNullAndCreatedAtBetween(
            @Param("userEmail") String userEmail,
            @Param("cryptoSymbol") String cryptoSymbol,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT r FROM Ranking r WHERE r.cryptoSymbol = 'BTC' AND r.ranked ='ON' ORDER BY r.yield DESC")
    Page<Ranking> findAllByBtcSelectedFields(Pageable pageable);

    @Query("SELECT r FROM Ranking r WHERE r.cryptoSymbol = 'ETH' AND r.ranked ='ON' ORDER BY r.yield DESC")
    Page<Ranking> findAllByEthSelectedFields(Pageable pageable);

    boolean existsByUserEmailAndCryptoSymbol(String userEmail, String cryptoSymbol);
}
