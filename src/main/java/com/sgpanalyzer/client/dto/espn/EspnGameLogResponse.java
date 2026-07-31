package com.sgpanalyzer.client.dto.espn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EspnGameLogResponse {

    private List<String> names;       // stat column names: ["rushingAttempts", "rushingYards", ...]
    private List<String> labels;      // abbreviated labels: ["CAR", "YDS", ...]
    private Map<String, Event> events; // eventId → event metadata
    private List<SeasonType> seasonTypes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        private String id;
        private String gameDate; // ISO format: "2024-09-08T17:00:00.000+00:00"
        private int week;
        private Opponent opponent;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Opponent {
        private String id;
        private String abbreviation;
        private String displayName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SeasonType {
        private String displayName;
        private List<Category> categories;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Category {
        private String type;          // "event" for per-game stats
        private String displayName;
        private List<GameStats> events;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GameStats {
        private String eventId;
        private List<String> stats;   // parallel to top-level "names" array
    }
}
