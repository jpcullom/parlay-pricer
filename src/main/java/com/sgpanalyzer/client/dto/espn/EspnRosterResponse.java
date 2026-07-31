package com.sgpanalyzer.client.dto.espn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnRosterResponse {

    private List<PositionGroup> athletes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PositionGroup {
        private String position; // "offense", "defense", "specialTeam"
        private List<Athlete> items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Athlete {
        private String id;
        private String fullName;
        private Position position;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Position {
        private String abbreviation; // "QB", "RB", "WR", "TE", etc.
        private String name;
    }
}
