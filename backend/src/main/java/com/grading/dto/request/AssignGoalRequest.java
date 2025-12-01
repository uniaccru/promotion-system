package com.grading.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignGoalRequest {
    @NotNull(message = "Goal ID is required")
    private Long goalId;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
}

