package com.sgpanalyzer.repository;

import com.sgpanalyzer.model.entity.GameLog;
import com.sgpanalyzer.model.enums.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameLogRepository extends JpaRepository<GameLog, Long> {

    List<GameLog> findByPlayerNameAndMarket(String playerName, Market market);

    List<GameLog> findByTeam(String team);

    List<GameLog> findByPlayerName(String playerName);

    List<GameLog> findByTeamAndMarket(String team, Market market);
}
