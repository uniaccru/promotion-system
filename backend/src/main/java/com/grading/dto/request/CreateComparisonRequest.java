package com.grading.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateComparisonRequest {
    @NotNull(message = "Calibration ID is required")
    private Long calibrationId;

    @NotNull(message = "Candidate A ID is required")
    private Long candidateAId;

    @NotNull(message = "Candidate B ID is required")
    private Long candidateBId;

    @NotNull(message = "Winner ID is required")
    private Long winnerId;
}



