package com.grading.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReturnForRevisionRequest {
    @NotBlank(message = "Comment is required when returning for revision")
    private String comment;
}
