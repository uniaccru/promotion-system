package com.grading.service.impl;

import com.grading.dto.response.EmployeeProfileResponse;
import com.grading.dto.response.EmployeeResponse;
import com.grading.entity.Employee;
import com.grading.entity.GradeHistory;
import com.grading.entity.ManagerEvaluation;
import com.grading.exception.ResourceNotFoundException;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.GradeHistoryRepository;
import com.grading.repository.ManagerEvaluationRepository;
import com.grading.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final GradeHistoryRepository gradeHistoryRepository;
    private final ManagerEvaluationRepository managerEvaluationRepository;

    @Override
    public EmployeeProfileResponse getEmployeeProfile(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));

        GradeHistory latestGrade = gradeHistoryRepository.findLatestByEmployeeId(employeeId).orElse(null);
        ManagerEvaluation latestEvaluation = managerEvaluationRepository.findLatestByEmployeeId(employeeId).orElse(null);
        List<GradeHistory> gradeHistories = gradeHistoryRepository.findByEmployeeIdOrderByChangedAtDesc(employeeId);

        EmployeeProfileResponse response = new EmployeeProfileResponse();
        response.setId(employee.getId());
        response.setFullName(employee.getFullName());
        response.setEmail(employee.getEmail());
        response.setRole(employee.getRole());
        response.setHireDate(employee.getHireDate());
        response.setDepartment(employee.getDepartment());
        response.setReviewPeriod(employee.getReviewPeriod());
        response.setCurrentGrade(latestGrade != null ? latestGrade.getNewGrade().getName() : "N/A");
        response.setLastReviewDate(latestEvaluation != null ? latestEvaluation.getCreatedAt() : null);
        response.setLastScore(latestEvaluation != null ? latestEvaluation.getScore() : null);
        response.setGradeChangesCount(gradeHistories.size());

        return response;
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
            .map(this::toEmployeeResponse)
            .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return toEmployeeResponse(employee);
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department).stream()
            .map(this::toEmployeeResponse)
            .collect(Collectors.toList());
    }

    private EmployeeResponse toEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setFullName(employee.getFullName());
        response.setEmail(employee.getEmail());
        response.setRole(employee.getRole());
        response.setHireDate(employee.getHireDate());
        response.setDepartment(employee.getDepartment());
        response.setReviewPeriod(employee.getReviewPeriod());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }
}