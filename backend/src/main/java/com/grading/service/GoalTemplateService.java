package com.grading.service;

import com.grading.dto.request.CreateGoalRequest;
import com.grading.dto.response.GoalTemplateResponse;

import java.util.List;

public interface GoalTemplateService {
    GoalTemplateResponse createGoalTemplate(CreateGoalRequest request);
    GoalTemplateResponse updateGoalTemplate(Long id, CreateGoalRequest request);
    void deleteGoalTemplate(Long id);
    GoalTemplateResponse getGoalTemplateById(Long id);
    List<GoalTemplateResponse> getAllGoalTemplates();
    List<GoalTemplateResponse> getGoalTemplatesByReviewPeriod(String reviewPeriod);
}

