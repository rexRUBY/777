package org.example.batch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.common.common.entity.Timestamped;
import org.example.common.subscriptions.entity.Subscribe;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
