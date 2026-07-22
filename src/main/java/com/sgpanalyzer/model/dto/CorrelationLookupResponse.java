package com.sgpanalyzer.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrelationLookupResponse {

    private String playerA;
    private String playerB;
    private double correlation;
    private int sampleSize;
}
