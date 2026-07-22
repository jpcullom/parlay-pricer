package com.sgpanalyzer.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    private int status;
    private String error;
    private String message;
    private String path;
}
