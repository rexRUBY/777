package org.example.ranking.repository;

import org.example.ranking.entity.Ranking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking,Long> {
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
