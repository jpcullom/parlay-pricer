package com.sgpanalyzer.model.dto;

import com.sgpanalyzer.model.enums.Recommendation;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParlayAnalysisResponse {

    private double correlation;
    private int fairOdds;
    private int sportsbookOdds;
    private double expectedValue;
    private Recommendation recommendation;
}
