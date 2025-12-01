package com.grading.service.impl;

import com.grading.dto.request.AssignGoalRequest;
import com.grading.dto.request.UpdateGoalStatusRequest;
import com.grading.dto.response.GoalResponse;
import com.grading.entity.Employee;
import com.grading.entity.Goal;
import com.grading.entity.GoalAssignment;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.GoalAssignmentRepository;
import com.grading.repository.GoalRepository;
import com.grading.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {
    private final GoalAssignmentRepository goalAssignmentRepository;
    private final GoalRepository goalRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public GoalResponse assignGoal(AssignGoalRequest request) {
        Goal goal = goalRepository.findById(request.getGoalId())
            .orElseThrow(() -> new RuntimeException("Goal template not found"));
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        GoalAssignment assignment = new GoalAssignment();
        assignment.setGoal(goal);
        assignment.setEmployee(employee);
        assignment.setDueDate(request.getDueDate());
        assignment.setStatus("in_progress");

        GoalAssignment saved = goalAssignmentRepository.save(assignment);
        return toGoalResponse(saved);
    }

    @Override
    @Transactional
    public GoalResponse updateGoalStatus(Long id, UpdateGoalStatusRequest request) {
        GoalAssignment assignment = goalAssignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Goal assignment not found"));

        assignment.setStatus(request.getStatus());

        GoalAssignment saved = goalAssignmentRepository.save(assignment);
        return toGoalResponse(saved);
    }

    @Override
    @Transactional
    public void deleteGoalAssignment(Long id) {
        goalAssignmentRepository.deleteById(id);
    }

    @Override
    public GoalResponse getGoalAssignmentById(Long id) {
        GoalAssignment assignment = goalAssignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Goal assignment not found"));
        return toGoalResponse(assignment);
    }

    @Override
    public List<GoalResponse> getGoalsByEmployeeId(Long employeeId) {
        return goalAssignmentRepository.findByEmployeeId(employeeId).stream()
            .map(this::toGoalResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<GoalResponse> getGoalsByEmployeeIdAndReviewPeriod(Long employeeId, String reviewPeriod) {
        return goalAssignmentRepository.findByEmployeeIdAndReviewPeriod(employeeId, reviewPeriod).stream()
            .map(this::toGoalResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<GoalResponse> getAllGoalAssignments() {
        return goalAssignmentRepository.findAllWithRelations().stream()
            .map(this::toGoalResponse)
            .collect(Collectors.toList());
    }

    private GoalResponse toGoalResponse(GoalAssignment assignment) {
        GoalResponse response = new GoalResponse();
        response.setId(assignment.getId());
        response.setGoalId(assignment.getGoal().getId());
        response.setGoalTitle(assignment.getGoal().getTitle());
        response.setGoalDescription(assignment.getGoal().getDescription());
        response.setGoalMetric(assignment.getGoal().getMetric());
        response.setEmployeeId(assignment.getEmployee().getId());
        response.setEmployeeName(assignment.getEmployee().getFullName());
        response.setDueDate(assignment.getDueDate());
        response.setStatus(assignment.getStatus());
        response.setReviewPeriod(assignment.getGoal().getReviewPeriod());
        response.setCreatedAt(assignment.getCreatedAt());
        response.setUpdatedAt(assignment.getUpdatedAt());
        return response;
    }
}