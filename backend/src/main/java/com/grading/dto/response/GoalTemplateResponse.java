package com.grading.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalTemplateResponse {
    private Long id;
    private String title;
    private String description;
    private String metric;
    private String reviewPeriod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

