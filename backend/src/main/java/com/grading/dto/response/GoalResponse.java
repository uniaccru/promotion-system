package com.grading.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private Long id;
    private Long goalId;
    private String goalTitle;
    private String goalDescription;
    private String goalMetric;
    private Long employeeId;
    private String employeeName;
    private LocalDate dueDate;
    private String status;
    private String reviewPeriod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



