package com.sgpanalyzer.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpectedValueResponse {

    private int fairOdds;
    private double expectedValue;
}
