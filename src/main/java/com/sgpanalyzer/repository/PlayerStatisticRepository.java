package com.sgpanalyzer.repository;

import com.sgpanalyzer.model.entity.PlayerStatistic;
import com.sgpanalyzer.model.enums.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerStatisticRepository extends JpaRepository<PlayerStatistic, Long> {

    Optional<PlayerStatistic> findByPlayerNameAndMarket(String playerName, Market market);

    List<PlayerStatistic> findByTeam(String team);

    List<PlayerStatistic> findByPlayerName(String playerName);
}
