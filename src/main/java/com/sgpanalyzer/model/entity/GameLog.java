package com.sgpanalyzer.model.entity;

import com.sgpanalyzer.model.enums.Market;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "game_logs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"game_id", "player_name", "market"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false, length = 64)
    private String gameId;

    @Column(name = "game_date", nullable = false)
    private LocalDate gameDate;

    @Column(name = "player_name", nullable = false, length = 128)
    private String playerName;

    @Column(nullable = false, length = 10)
    private String team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Market market;

    @Column(name = "stat_value", nullable = false)
    private double statValue;
}
