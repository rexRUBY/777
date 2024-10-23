package org.example.common.crypto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.example.common.common.entity.Timestamped;

@Entity
@Getter
public class Crypto extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "description")
    private String description;

}
