package com.grading.service;

import com.grading.dto.response.EmployeeProfileResponse;
import com.grading.dto.response.EmployeeResponse;
import com.grading.entity.Employee;

import java.util.List;

public interface EmployeeService {
    EmployeeProfileResponse getEmployeeProfile(Long employeeId);
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse getEmployeeById(Long id);
    List<EmployeeResponse> getEmployeesByDepartment(String department);
}