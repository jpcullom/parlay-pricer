package com.sgpanalyzer.model.entity;

import com.sgpanalyzer.model.enums.Market;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "player_correlations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"player_a", "player_b", "market_a", "market_b"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerCorrelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_a", nullable = false, length = 128)
    private String playerA;

    @Column(name = "player_b", nullable = false, length = 128)
    private String playerB;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_a", nullable = false, length = 32)
    private Market marketA;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_b", nullable = false, length = 32)
    private Market marketB;

    @Column(nullable = false)
    private double correlation;

    @Column(name = "sample_size", nullable = false)
    private int sampleSize;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;
}
