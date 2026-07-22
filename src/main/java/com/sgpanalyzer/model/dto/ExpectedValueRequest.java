package com.sgpanalyzer.model.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpectedValueRequest {

    @NotNull(message = "Fair probability is required")
    @DecimalMin(value = "0.01", message = "Probability must be greater than 0")
    @DecimalMax(value = "0.99", message = "Probability must be less than 1")
    private Double fairProbability;

    @NotNull(message = "Sportsbook odds are required")
    private Integer sportsbookOdds;
}
