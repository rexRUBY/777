package org.example.ranking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.common.common.entity.Timestamped;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ranking", indexes = {
        @Index(name = "idx_ranking_symbol", columnList = "crypto_symbol"),
        @Index(name = "idx_ranking_yield",columnList = "yield"),
        @Index(name = "idx_ranking_id",columnList = "id"),
        @Index(name = "idx_ranking_ranked", columnList = "ranked")
},uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_email", "crypto_symbol"}) // user_email과 crypto_symbol의 조합으로 유니크 제약 조건 설정
})
public class Ranking extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_rank")
    private Long userRank;

    @Column(name = "user_email")
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "ranked")
    private Ranked ranked = Ranked.ON;

    @Column(name = "crypto_symbol")
    private String cryptoSymbol;

    @Column(name = "yield")
    private Double yield;

    public Ranking(String userEmail, String cryptoSymbol, Double yield) {
        this.userEmail = userEmail;
        this.cryptoSymbol = cryptoSymbol;
        this.yield = yield;
    }

    public void update(Long count) {
        this.userRank = count;
        this.ranked = Ranked.OFF;
    }
}
