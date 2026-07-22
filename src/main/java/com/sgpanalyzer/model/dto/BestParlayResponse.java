package com.sgpanalyzer.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BestParlayResponse {

    private List<String> legs;
    private double correlation;
}
