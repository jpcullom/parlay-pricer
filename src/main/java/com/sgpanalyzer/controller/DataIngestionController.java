package com.sgpanalyzer.controller;

import com.sgpanalyzer.service.DataIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
@Tag(name = "Data Ingestion", description = "Ingest player game log data from ESPN")
public class DataIngestionController {

    private final DataIngestionService dataIngestionService;

    @PostMapping("/ingest")
    @Operation(summary = "Ingest game logs for a team/season",
            description = "Pulls player game-by-game stats from ESPN for the specified team and season, "
                    + "and persists them as GameLog records. Idempotent — duplicate records are skipped.")
    public ResponseEntity<Map<String, Object>> ingestData(
            @RequestParam String team,
            @RequestParam(defaultValue = "2024") int season) {

        int savedCount = dataIngestionService.ingestPlayerStats(team, season);

        return ResponseEntity.ok(Map.of(
                "team", team.toUpperCase(),
                "season", season,
                "recordsSaved", savedCount
        ));
    }
}
