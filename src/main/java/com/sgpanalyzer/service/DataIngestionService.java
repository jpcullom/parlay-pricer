package com.sgpanalyzer.service;

import com.sgpanalyzer.client.EspnClient;
import com.sgpanalyzer.client.dto.espn.EspnGameLogResponse;
import com.sgpanalyzer.client.dto.espn.EspnRosterResponse;
import com.sgpanalyzer.model.entity.GameLog;
import com.sgpanalyzer.model.enums.Market;
import com.sgpanalyzer.repository.GameLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionService {

    private final GameLogRepository gameLogRepository;
    private final EspnClient espnClient;

    /**
     * Ingests player game log data for a given team and season.
     * Pulls from ESPN's public API, maps to GameLog entities, and persists to the database.
     *
     * @param teamAbbreviation NFL team abbreviation (e.g., "ATL")
     * @param season           NFL season year (e.g., 2024)
     * @return number of game log records saved
     */
    @Transactional
    public int ingestPlayerStats(String teamAbbreviation, int season) {
        log.info("Starting data ingestion for team {} season {}", teamAbbreviation, season);

        // Step 1: Resolve team abbreviation to ESPN team ID
        String teamId = espnClient.resolveTeamId(teamAbbreviation);
        log.info("Resolved team {} to ESPN ID {}", teamAbbreviation, teamId);

        // Step 2: Fetch skill-position roster (QB, RB, WR, TE)
        List<EspnRosterResponse.Athlete> athletes = espnClient.fetchSkillPositionRoster(teamId);
        log.info("Found {} skill-position players for {}", athletes.size(), teamAbbreviation);

        Map<String, Market> statMapping = EspnClient.getStatToMarketMapping();
        int totalSaved = 0;

        // Step 3: For each player, fetch game log and persist stats
        for (EspnRosterResponse.Athlete athlete : athletes) {
            try {
                int saved = ingestAthleteGameLog(athlete, teamAbbreviation, season, statMapping);
                totalSaved += saved;
            } catch (Exception e) {
                log.warn("Failed to ingest game log for {} (ID {}): {}",
                        athlete.getFullName(), athlete.getId(), e.getMessage());
            }
        }

        log.info("Data ingestion complete for {} season {}. Saved {} game log records.",
                teamAbbreviation, season, totalSaved);
        return totalSaved;
    }

    private int ingestAthleteGameLog(EspnRosterResponse.Athlete athlete,
                                     String teamAbbreviation,
                                     int season,
                                     Map<String, Market> statMapping) {
        EspnGameLogResponse gameLog = espnClient.fetchAthleteGameLog(athlete.getId(), season);

        if (gameLog == null || gameLog.getNames() == null || gameLog.getSeasonTypes() == null) {
            log.debug("No game log data for {} (ID {})", athlete.getFullName(), athlete.getId());
            return 0;
        }

        List<String> statNames = gameLog.getNames();
        Map<String, EspnGameLogResponse.Event> eventsMap = gameLog.getEvents();
        int saved = 0;

        for (EspnGameLogResponse.SeasonType seasonType : gameLog.getSeasonTypes()) {
            if (seasonType.getCategories() == null) continue;

            for (EspnGameLogResponse.Category category : seasonType.getCategories()) {
                if (!"event".equals(category.getType()) || category.getEvents() == null) continue;

                for (EspnGameLogResponse.GameStats gameStats : category.getEvents()) {
                    String eventId = gameStats.getEventId();
                    List<String> stats = gameStats.getStats();
                    EspnGameLogResponse.Event event = eventsMap != null ? eventsMap.get(eventId) : null;

                    LocalDate gameDate = parseGameDate(event);

                    // Map each stat to our Market enum and save
                    for (int i = 0; i < statNames.size() && i < stats.size(); i++) {
                        String espnStatName = statNames.get(i);
                        Market market = statMapping.get(espnStatName);
                        if (market == null) continue; // Not a market we care about

                        String statValue = stats.get(i);
                        if (statValue == null || "-".equals(statValue)) continue;

                        double value;
                        try {
                            value = Double.parseDouble(statValue.replace(",", ""));
                        } catch (NumberFormatException e) {
                            continue;
                        }

                        GameLog entry = GameLog.builder()
                                .gameId(eventId)
                                .gameDate(gameDate)
                                .playerName(athlete.getFullName())
                                .team(teamAbbreviation.toUpperCase())
                                .market(market)
                                .statValue(value)
                                .build();

                        try {
                            gameLogRepository.save(entry);
                            saved++;
                        } catch (DataIntegrityViolationException e) {
                            // Duplicate (gameId, playerName, market) — skip
                            log.trace("Skipping duplicate: {} {} {} {}",
                                    eventId, athlete.getFullName(), market, value);
                        }
                    }
                }
            }
        }

        log.debug("Saved {} game log records for {}", saved, athlete.getFullName());
        return saved;
    }

    private LocalDate parseGameDate(EspnGameLogResponse.Event event) {
        if (event == null || event.getGameDate() == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(event.getGameDate()).toLocalDate();
        } catch (Exception e) {
            log.warn("Failed to parse game date: {}", event.getGameDate());
            return null;
        }
    }
}
