package com.sgpanalyzer.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParlayAnalysisRequest {

    @NotNull(message = "Legs are required")
    @Size(min = 2, message = "At least 2 legs are required for a parlay")
    @Valid
    private List<ParlayLeg> legs;

    @NotNull(message = "Sportsbook odds are required")
    private Integer sportsbookOdds;
}
