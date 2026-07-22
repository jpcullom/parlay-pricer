package com.sgpanalyzer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class OddsApiClient {

    private final RestClient restClient;

    public OddsApiClient(@Value("${sgp.clients.odds-api.base-url}") String baseUrl,
                          @Value("${sgp.clients.odds-api.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .build();
    }

    /**
     * Fetches current odds for a given sport and market type.
     *
     * @param sport  e.g., "americanfootball_nfl"
     * @param market e.g., "player_props", "h2h", "spreads"
     * @return Raw odds data
     */
    public Object fetchCurrentOdds(String sport, String market) {
        // TODO: Implement API call to The Odds API
        //  GET /sports/{sport}/odds/?regions=us&markets={market}&oddsFormat=american
        //
        //  Example:
        //  return restClient.get()
        //      .uri("/sports/{sport}/odds/?regions=us&markets={market}&oddsFormat=american", sport, market)
        //      .retrieve()
        //      .body(OddsApiResponse.class);
        //
        //  Define OddsApiResponse DTO to match the API's JSON structure.
        //  See: https://the-odds-api.com/liveapi/guides/v4/

        log.warn("OddsApiClient.fetchCurrentOdds() not yet implemented");
        return null;
    }

    /**
     * Fetches player prop odds for a specific event.
     *
     * @param eventId The event/game identifier
     * @return Player prop odds data
     */
    public Object fetchPlayerProps(String eventId) {
        // TODO: Implement API call
        //  GET /sports/americanfootball_nfl/events/{eventId}/odds?regions=us&markets=player_pass_yds,player_rush_yds,player_reception_yds
        //
        //  return restClient.get()
        //      .uri("/sports/americanfootball_nfl/events/{eventId}/odds?regions=us&markets=player_pass_yds,player_rush_yds,player_reception_yds", eventId)
        //      .retrieve()
        //      .body(PlayerPropsResponse.class);

        log.warn("OddsApiClient.fetchPlayerProps() not yet implemented");
        return null;
    }
}
