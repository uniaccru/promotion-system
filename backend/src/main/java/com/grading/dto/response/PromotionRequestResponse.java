package com.grading.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequestResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long requestedGradeId;
    private String requestedGradeName;
    private Long submittedById;
    private String submittedByName;
    private String justification;
    private String evidence;
    private String reviewPeriod;
    private String status;
    private String hrComment;
    private LocalDateTime createdAt;
}




