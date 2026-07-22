package com.sgpanalyzer.controller;

import com.sgpanalyzer.model.dto.ExpectedValueRequest;
import com.sgpanalyzer.model.dto.ExpectedValueResponse;
import com.sgpanalyzer.service.ExpectedValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ev")
@RequiredArgsConstructor
@Tag(name = "Expected Value", description = "Expected value calculation endpoints")
public class ExpectedValueController {

    private final ExpectedValueService expectedValueService;

    @PostMapping
    @Operation(summary = "Calculate expected value",
               description = "Given a fair probability and sportsbook odds, computes the fair odds and expected value")
    public ResponseEntity<ExpectedValueResponse> calculateExpectedValue(
            @Valid @RequestBody ExpectedValueRequest request) {
        ExpectedValueResponse response = expectedValueService.calculateEV(request);
        return ResponseEntity.ok(response);
    }
}
