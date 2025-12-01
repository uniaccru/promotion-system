package com.grading.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CalibrationResponse {
    private Long id;
    private Long gradeId;
    private String gradeName;
    private Long createdById;
    private String createdByName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> evaluatorIds;
    private List<String> evaluatorNames;
    private List<Long> promotionRequestIds;
    private Integer candidateCount;
}



