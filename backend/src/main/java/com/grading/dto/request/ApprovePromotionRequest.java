package com.grading.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ApprovePromotionRequest {
    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "approved|rejected", message = "Decision must be 'approved' or 'rejected'")
    private String decision;
    
    private String comment;
}
