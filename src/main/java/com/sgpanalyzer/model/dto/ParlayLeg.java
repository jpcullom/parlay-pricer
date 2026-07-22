package com.sgpanalyzer.model.dto;

import com.sgpanalyzer.model.enums.Market;
import com.sgpanalyzer.model.enums.Selection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParlayLeg {

    @NotBlank(message = "Player name is required")
    private String player;

    @NotNull(message = "Market is required")
    private Market market;

    @Positive(message = "Line must be positive")
    private double line;

    @NotNull(message = "Selection is required")
    private Selection selection;
}
