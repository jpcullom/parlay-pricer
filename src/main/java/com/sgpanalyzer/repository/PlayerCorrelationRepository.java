package com.sgpanalyzer.repository;

import com.sgpanalyzer.model.entity.PlayerCorrelation;
import com.sgpanalyzer.model.enums.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerCorrelationRepository extends JpaRepository<PlayerCorrelation, Long> {

    Optional<PlayerCorrelation> findByPlayerAAndPlayerBAndMarketAAndMarketB(
            String playerA, String playerB, Market marketA, Market marketB);

    @Query("SELECT pc FROM PlayerCorrelation pc WHERE pc.playerA = :player OR pc.playerB = :player")
    List<PlayerCorrelation> findAllByPlayer(@Param("player") String player);

    @Query("""
            SELECT pc FROM PlayerCorrelation pc
            WHERE pc.playerA IN :players AND pc.playerB IN :players
            """)
    List<PlayerCorrelation> findAllByPlayersIn(@Param("players") List<String> players);

    @Query("""
            SELECT pc FROM PlayerCorrelation pc
            ORDER BY ABS(pc.correlation) DESC
            LIMIT :limit
            """)
    List<PlayerCorrelation> findTopCorrelated(@Param("limit") int limit);
}
