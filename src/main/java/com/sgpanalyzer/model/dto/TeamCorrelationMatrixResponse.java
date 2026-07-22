package com.sgpanalyzer.model.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamCorrelationMatrixResponse {

    private String team;
    private List<String> players;
    private double[][] matrix;
}
