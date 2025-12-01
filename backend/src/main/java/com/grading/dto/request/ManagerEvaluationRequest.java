package com.grading.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ManagerEvaluationRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Review period is required")
    private String reviewPeriod;

    @NotNull(message = "Score is required")
    private BigDecimal score;

    @NotBlank(message = "Comment is required")
    private String comment;

    @NotNull(message = "Nomination status is required")
    private Boolean nominatedForPromotion;
}