package com.sgpanalyzer.controller;

import com.sgpanalyzer.model.dto.TeamCorrelationMatrixResponse;
import com.sgpanalyzer.service.CorrelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Team-level correlation endpoints")
public class TeamController {

    private final CorrelationService correlationService;

    @GetMapping("/{team}/correlations")
    @Operation(summary = "Get team correlation matrix",
               description = "Returns the full NxN correlation matrix for all tracked players on a team")
    public ResponseEntity<TeamCorrelationMatrixResponse> getTeamCorrelationMatrix(
            @PathVariable String team) {
        TeamCorrelationMatrixResponse response = correlationService.getTeamCorrelationMatrix(team);
        return ResponseEntity.ok(response);
    }
}
