package com.grading.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateCalibrationPackageRequest {
    @NotNull(message = "Grade ID is required")
    private Long gradeId;

    @NotEmpty(message = "At least 3 promotion request IDs are required")
    @Size(min = 3, message = "At least 3 promotion requests are required")
    private List<Long> promotionRequestIds;

    @NotEmpty(message = "At least 2 evaluator IDs are required")
    @Size(min = 2, max = 2, message = "Exactly 2 evaluators are required")
    private List<Long> evaluatorIds;
}



