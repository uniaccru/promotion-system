package com.grading.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private LocalDate hireDate;
    private String department;
    private String reviewPeriod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}




