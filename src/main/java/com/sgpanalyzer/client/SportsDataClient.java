package com.sgpanalyzer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class SportsDataClient {

    private final RestClient restClient;

    public SportsDataClient(@Value("${sgp.clients.sports-data.base-url}") String baseUrl,
                             @Value("${sgp.clients.sports-data.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-apisports-key", apiKey)
                .build();
    }

    /**
     * Fetches game-by-game statistics for a player in a given season.
     *
     * @param playerName The player's full name (e.g., "Patrick Mahomes")
     * @param season     The NFL season year (e.g., 2024)
     * @return List of game log data
     */
    public Object fetchPlayerGameLogs(String playerName, int season) {
        // TODO: Implement API call to API-Sports
        //  GET /players/statistics?season={season}&search={playerName}
        //
        //  Steps:
        //  1. Search for player ID by name
        //  2. Fetch game-by-game stats for that player ID
        //  3. Map response to internal DTOs
        //
        //  Example:
        //  return restClient.get()
        //      .uri("/players/statistics?season={season}&id={playerId}", season, playerId)
        //      .retrieve()
        //      .body(SportsDataResponse.class);
        //
        //  Define SportsDataResponse DTO to match the API-Sports JSON structure.
        //  See: https://api-sports.io/documentation/nfl/v1

        log.warn("SportsDataClient.fetchPlayerGameLogs() not yet implemented");
        return null;
    }

    /**
     * Fetches all games for a given season.
     *
     * @param season The NFL season year
     * @return Game schedule and results
     */
    public Object fetchGames(int season) {
        // TODO: Implement API call
        //  GET /games?season={season}
        //
        //  return restClient.get()
        //      .uri("/games?season={season}", season)
        //      .retrieve()
        //      .body(GamesResponse.class);

        log.warn("SportsDataClient.fetchGames() not yet implemented");
        return null;
    }
}
