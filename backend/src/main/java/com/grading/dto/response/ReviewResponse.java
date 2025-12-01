package com.grading.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long evaluatorId;
    private String evaluatorName;
    private Long employeeId;
    private String employeeName;
    private String reviewPeriod;
    private BigDecimal score;
    private String comment;
    private Boolean nominatedForPromotion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}




