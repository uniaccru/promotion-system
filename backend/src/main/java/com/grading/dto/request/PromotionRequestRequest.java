package com.grading.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromotionRequestRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Requested grade ID is required")
    private Long requestedGradeId;

    @NotBlank(message = "Justification is required")
    private String justification;

    @NotBlank(message = "Evidence is required")
    private String evidence;

    @NotBlank(message = "Review period is required")
    private String reviewPeriod;
}