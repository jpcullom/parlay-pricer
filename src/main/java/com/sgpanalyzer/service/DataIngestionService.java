package com.sgpanalyzer.service;

import com.sgpanalyzer.client.EspnClient;
import com.sgpanalyzer.client.OddsApiClient;
import com.sgpanalyzer.client.SportsDataClient;
import com.sgpanalyzer.repository.GameLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService {

    private final GameLogRepository gameLogRepository;
    private final OddsApiClient oddsApiClient;
    private final SportsDataClient sportsDataClient;
    private final EspnClient espnClient;

    /**
     * Ingests player game log data for a given NFL season.
     * Pulls from external APIs, maps to GameLog entities, and persists to the database.
     */
    @Transactional
    public void ingestPlayerStats(int season) {
        log.info("Starting data ingestion for season {}", season);

        // TODO: Step 1 — Fetch team rosters from ESPN API
        //  List<TeamRoster> rosters = espnClient.fetchAllTeamRosters();
        //  This gives us the list of players to pull game logs for.

        // TODO: Step 2 — For each player, fetch game-by-game stats from SportsData API
        //  for (Player player : allPlayers) {
        //      List<GameLogData> gameLogs = sportsDataClient.fetchPlayerGameLogs(player.getName(), season);
        //
        //      for (GameLogData data : gameLogs) {
        //          // Map external API response to GameLog entity
        //          GameLog gameLog = GameLog.builder()
        //              .gameId(data.getGameId())
        //              .gameDate(data.getGameDate())
        //              .playerName(player.getName())
        //              .team(player.getTeam())
        //              .market(mapToMarket(data.getStatCategory()))
        //              .statValue(data.getValue())
        //              .build();
        //
        //          // Upsert: skip if (gameId, playerName, market) already exists
        //          gameLogRepository.save(gameLog);
        //      }
        //  }

        // TODO: Step 3 — Optionally fetch current odds from The Odds API
        //  This could be used to pre-populate available markets/lines
        //  oddsApiClient.fetchCurrentOdds("americanfootball_nfl", "player_props");

        // TODO: Step 4 — Log summary of ingested records
        //  log.info("Ingested {} game logs for season {}", count, season);

        log.info("Data ingestion for season {} complete (not yet implemented)", season);
    }
}
