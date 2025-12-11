package com.grading.service;

import com.grading.dto.request.AssignGoalRequest;
import com.grading.dto.request.UpdateGoalStatusRequest;
import com.grading.dto.response.GoalResponse;

import java.util.List;

public interface GoalService {
    GoalResponse assignGoal(AssignGoalRequest request);
    GoalResponse updateGoalStatus(Long id, UpdateGoalStatusRequest request);
    void deleteGoalAssignment(Long id);
    GoalResponse getGoalAssignmentById(Long id);
    List<GoalResponse> getGoalsByEmployeeId(Long employeeId);
    List<GoalResponse> getGoalsByEmployeeIdAndReviewPeriod(Long employeeId, String reviewPeriod);
    List<GoalResponse> getGoalsByEmployeeIdAndStatus(Long employeeId, String status);
    List<GoalResponse> getAllGoalAssignments();
}