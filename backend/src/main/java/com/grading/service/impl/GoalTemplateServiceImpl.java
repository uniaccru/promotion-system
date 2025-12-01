package com.grading.service.impl;

import com.grading.dto.request.CreateGoalRequest;
import com.grading.dto.response.GoalTemplateResponse;
import com.grading.entity.Goal;
import com.grading.repository.GoalRepository;
import com.grading.service.GoalTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalTemplateServiceImpl implements GoalTemplateService {
    private final GoalRepository goalRepository;

    @Override
    @Transactional
    public GoalTemplateResponse createGoalTemplate(CreateGoalRequest request) {
        Goal goal = new Goal();
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setMetric(request.getMetric());
        goal.setReviewPeriod(request.getReviewPeriod());

        Goal saved = goalRepository.save(goal);
        return toGoalTemplateResponse(saved);
    }

    @Override
    @Transactional
    public GoalTemplateResponse updateGoalTemplate(Long id, CreateGoalRequest request) {
        Goal goal = goalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Goal template not found"));

        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setMetric(request.getMetric());
        goal.setReviewPeriod(request.getReviewPeriod());

        Goal saved = goalRepository.save(goal);
        return toGoalTemplateResponse(saved);
    }

    @Override
    @Transactional
    public void deleteGoalTemplate(Long id) {
        goalRepository.deleteById(id);
    }

    @Override
    public GoalTemplateResponse getGoalTemplateById(Long id) {
        Goal goal = goalRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Goal template not found"));
        return toGoalTemplateResponse(goal);
    }

    @Override
    public List<GoalTemplateResponse> getAllGoalTemplates() {
        return goalRepository.findAll().stream()
            .map(this::toGoalTemplateResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<GoalTemplateResponse> getGoalTemplatesByReviewPeriod(String reviewPeriod) {
        return goalRepository.findByReviewPeriod(reviewPeriod).stream()
            .map(this::toGoalTemplateResponse)
            .collect(Collectors.toList());
    }

    private GoalTemplateResponse toGoalTemplateResponse(Goal goal) {
        GoalTemplateResponse response = new GoalTemplateResponse();
        response.setId(goal.getId());
        response.setTitle(goal.getTitle());
        response.setDescription(goal.getDescription());
        response.setMetric(goal.getMetric());
        response.setReviewPeriod(goal.getReviewPeriod());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());
        return response;
    }
}

