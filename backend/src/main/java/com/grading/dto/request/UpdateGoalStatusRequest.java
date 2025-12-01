package com.grading.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateGoalStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
}

