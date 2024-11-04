package org.example.common.crypto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.common.common.entity.Timestamped;

@Entity
@Getter
@NoArgsConstructor
public class Crypto extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "description")
    private String description;

    public Crypto(long l) {
        this.id=l;
    }


}
