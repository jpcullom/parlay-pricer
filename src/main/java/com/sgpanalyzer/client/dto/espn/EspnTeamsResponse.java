package com.sgpanalyzer.client.dto.espn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnTeamsResponse {

    private List<Sport> sports;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sport {
        private List<League> leagues;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class League {
        private List<TeamWrapper> teams;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TeamWrapper {
        private Team team;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Team {
        private String id;
        private String abbreviation;
        private String displayName;
    }
}
