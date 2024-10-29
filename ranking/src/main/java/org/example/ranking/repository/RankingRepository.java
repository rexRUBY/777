package org.example.ranking.repository;

import org.example.ranking.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RankingRepository extends JpaRepository<Ranking,Long> {
    boolean existsByUserEmailAndCryptoSymbolAndCreatedAt(String userEmail, String cryptoSymbol, LocalDateTime time);
    boolean existsByUserEmailAndCryptoSymbolAndCreatedAtAndUserRankNotNull(String userEmail, String cryptoSymbol, LocalDateTime time);
}
