package com.sgpanalyzer.controller;

import com.sgpanalyzer.model.dto.BestParlayResponse;
import com.sgpanalyzer.model.dto.ParlayAnalysisRequest;
import com.sgpanalyzer.model.dto.ParlayAnalysisResponse;
import com.sgpanalyzer.service.CorrelationService;
import com.sgpanalyzer.service.ParlayAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parlays")
@RequiredArgsConstructor
@Tag(name = "Parlays", description = "Same Game Parlay analysis endpoints")
public class ParlayController {

    private final ParlayAnalysisService parlayAnalysisService;
    private final CorrelationService correlationService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze a same-game parlay",
               description = "Computes correlation, fair odds, and expected value for a set of parlay legs against sportsbook odds")
    public ResponseEntity<ParlayAnalysisResponse> analyzeParlay(
            @Valid @RequestBody ParlayAnalysisRequest request) {
        ParlayAnalysisResponse response = parlayAnalysisService.analyzeParlay(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/best")
    @Operation(summary = "Get best correlated parlays",
               description = "Returns the top most strongly correlated player pairs, optionally filtered by team")
    public ResponseEntity<List<BestParlayResponse>> getBestParlays(
            @RequestParam(required = false) String team,
            @RequestParam(defaultValue = "10") int limit) {
        List<BestParlayResponse> response = correlationService.getBestCorrelatedParlays(team, limit);
        return ResponseEntity.ok(response);
    }
}
