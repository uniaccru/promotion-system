package com.grading.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalAssignmentRequest {
    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Metric is required")
    private String metric;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotBlank(message = "Status is required")
    private String status;

    @NotBlank(message = "Review period is required")
    private String reviewPeriod;
}