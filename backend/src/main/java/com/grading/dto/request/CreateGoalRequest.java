package com.grading.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGoalRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Metric is required")
    private String metric;

    @NotBlank(message = "Review period is required")
    private String reviewPeriod;
}

