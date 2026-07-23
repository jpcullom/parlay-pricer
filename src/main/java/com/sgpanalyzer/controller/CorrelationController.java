package com.sgpanalyzer.controller;

import com.sgpanalyzer.model.dto.CorrelationLookupResponse;
import com.sgpanalyzer.model.enums.Market;
import com.sgpanalyzer.service.CorrelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/correlations")
@RequiredArgsConstructor
@Tag(name = "Correlations", description = "Player correlation lookup endpoints")
public class CorrelationController {

    private final CorrelationService correlationService;

    @GetMapping
    @Operation(summary = "Look up correlation between two players",
               description = "Returns the precomputed Pearson correlation between two players for the specified markets")
    public ResponseEntity<CorrelationLookupResponse> getCorrelation(
            @RequestParam String playerA,
            @RequestParam String playerB,
            @RequestParam Market marketA,
            @RequestParam Market marketB) {
        CorrelationLookupResponse response = correlationService.getCorrelation(
                playerA, playerB, marketA, marketB);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rebuild")
    @Operation(summary = "Trigger correlation rebuild",
               description = "Recomputes all pairwise correlations and player statistics from game log data")
    public ResponseEntity<Map<String, String>> rebuildCorrelations() {
        correlationService.rebuildCorrelations();
        return ResponseEntity.ok(Map.of("status", "Correlation rebuild complete"));
    }
}
