package com.sgpanalyzer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class EspnClient {

    private final RestClient restClient;

    public EspnClient(@Value("${sgp.clients.espn.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Fetches the roster for a given NFL team.
     *
     * @param team Team abbreviation (e.g., "KC", "SF", "BUF")
     * @return Team roster data
     */
    public Object fetchTeamRoster(String team) {
        // TODO: Implement API call to ESPN public API
        //  GET /teams/{teamId}/roster
        //
        //  Steps:
        //  1. Map team abbreviation to ESPN team ID
        //  2. Fetch roster
        //  3. Map to internal DTOs
        //
        //  Example:
        //  return restClient.get()
        //      .uri("/teams/{teamId}/roster", espnTeamId)
        //      .retrieve()
        //      .body(EspnRosterResponse.class);
        //
        //  Note: ESPN's public API is undocumented but widely used.
        //  Common base: https://site.api.espn.com/apis/site/v2/sports/football/nfl

        log.warn("EspnClient.fetchTeamRoster() not yet implemented");
        return null;
    }

    /**
     * Fetches game results for a team in a given season.
     *
     * @param team   Team abbreviation
     * @param season The NFL season year
     * @return Game results data
     */
    public Object fetchGameResults(String team, int season) {
        // TODO: Implement API call
        //  GET /teams/{teamId}/schedule?season={season}
        //
        //  return restClient.get()
        //      .uri("/teams/{teamId}/schedule?season={season}", espnTeamId, season)
        //      .retrieve()
        //      .body(EspnScheduleResponse.class);

        log.warn("EspnClient.fetchGameResults() not yet implemented");
        return null;
    }

    /**
     * Fetches all NFL team information.
     *
     * @return All teams with IDs and abbreviations
     */
    public Object fetchAllTeams() {
        // TODO: Implement API call
        //  GET /teams
        //
        //  return restClient.get()
        //      .uri("/teams")
        //      .retrieve()
        //      .body(EspnTeamsResponse.class);

        log.warn("EspnClient.fetchAllTeams() not yet implemented");
        return null;
    }
}
