package com.sgpanalyzer.model.entity;

import com.sgpanalyzer.model.enums.Market;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "player_statistics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"player_name", "market"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", nullable = false, length = 128)
    private String playerName;

    @Column(nullable = false, length = 10)
    private String team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Market market;

    @Column(nullable = false)
    private double mean;

    @Column(name = "std_dev", nullable = false)
    private double stdDev;

    @Column(name = "sample_size", nullable = false)
    private int sampleSize;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
