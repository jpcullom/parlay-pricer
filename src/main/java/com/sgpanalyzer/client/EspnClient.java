package com.sgpanalyzer.client;

import com.sgpanalyzer.client.dto.espn.EspnGameLogResponse;
import com.sgpanalyzer.client.dto.espn.EspnRosterResponse;
import com.sgpanalyzer.client.dto.espn.EspnTeamsResponse;
import com.sgpanalyzer.model.enums.Market;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
@Slf4j
public class EspnClient {

    private static final Set<String> SKILL_POSITIONS = Set.of("QB", "RB", "WR", "TE");

    private static final Map<String, Market> STAT_TO_MARKET = Map.ofEntries(
            Map.entry("passingYards", Market.PASSING_YARDS),
            Map.entry("completions", Market.COMPLETIONS),
            Map.entry("passingCompletions", Market.COMPLETIONS),
            Map.entry("passingTouchdowns", Market.PASSING_TOUCHDOWNS),
            Map.entry("rushingYards", Market.RUSHING_YARDS),
            Map.entry("rushingAttempts", Market.CARRIES),
            Map.entry("carries", Market.CARRIES),
            Map.entry("rushingTouchdowns", Market.RUSHING_TOUCHDOWNS),
            Map.entry("receivingYards", Market.RECEIVING_YARDS),
            Map.entry("receptions", Market.RECEPTIONS),
            Map.entry("receivingTargets", Market.TARGETS)
    );

    private final RestClient restClient;
    private final RestClient gameLogClient;

    public EspnClient(@Value("${sgp.clients.espn.base-url}") String baseUrl,
                       @Value("${sgp.clients.espn.gamelog-base-url}") String gameLogBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.gameLogClient = RestClient.builder()
                .baseUrl(gameLogBaseUrl)
                .build();
    }

    /**
     * Fetches all NFL teams and returns the full response.
     */
    public EspnTeamsResponse fetchAllTeams() {
        log.debug("Fetching all NFL teams from ESPN");
        return restClient.get()
                .uri("/teams")
                .retrieve()
                .body(EspnTeamsResponse.class);
    }

    /**
     * Resolves a team abbreviation (e.g., "ATL") to an ESPN team ID.
     */
    public String resolveTeamId(String abbreviation) {
        EspnTeamsResponse response = fetchAllTeams();
        if (response == null || response.getSports() == null) {
            throw new IllegalStateException("Failed to fetch teams from ESPN");
        }

        return response.getSports().stream()
                .flatMap(sport -> sport.getLeagues().stream())
                .flatMap(league -> league.getTeams().stream())
                .map(EspnTeamsResponse.TeamWrapper::getTeam)
                .filter(team -> abbreviation.equalsIgnoreCase(team.getAbbreviation()))
                .map(EspnTeamsResponse.Team::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown team abbreviation: " + abbreviation));
    }

    /**
     * Fetches the roster for a team, filtered to skill positions (QB, RB, WR, TE).
     *
     * @param teamId ESPN team ID (numeric string)
     * @return list of skill-position athletes
     */
    public List<EspnRosterResponse.Athlete> fetchSkillPositionRoster(String teamId) {
        log.debug("Fetching roster for team ID {}", teamId);
        EspnRosterResponse response = restClient.get()
                .uri("/teams/{teamId}/roster", teamId)
                .retrieve()
                .body(EspnRosterResponse.class);

        if (response == null || response.getAthletes() == null) {
            return List.of();
        }

        return response.getAthletes().stream()
                .filter(group -> "offense".equalsIgnoreCase(group.getPosition()))
                .flatMap(group -> group.getItems().stream())
                .filter(athlete -> athlete.getPosition() != null
                        && SKILL_POSITIONS.contains(athlete.getPosition().getAbbreviation()))
                .toList();
    }

    /**
     * Fetches the game log for an athlete in a given season.
     * Uses the ESPN web API (different base URL from the main site API).
     *
     * @param athleteId ESPN athlete ID
     * @param season    NFL season year (e.g., 2024)
     */
    public EspnGameLogResponse fetchAthleteGameLog(String athleteId, int season) {
        log.debug("Fetching game log for athlete {} season {}", athleteId, season);
        return gameLogClient.get()
                .uri("/athletes/{athleteId}/gamelog?season={season}", athleteId, season)
                .retrieve()
                .body(EspnGameLogResponse.class);
    }

    /**
     * Returns the mapping from ESPN stat names to Market enum values.
     */
    public static Map<String, Market> getStatToMarketMapping() {
        return STAT_TO_MARKET;
    }
}
