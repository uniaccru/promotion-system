package com.grading.service;

import com.grading.dto.request.ManagerEvaluationRequest;
import com.grading.dto.response.ReviewResponse;

import java.util.List;

public interface ManagerEvaluationService {
    ReviewResponse createEvaluation(ManagerEvaluationRequest request, Long evaluatorId);
    ReviewResponse updateEvaluation(Long id, ManagerEvaluationRequest request);
    ReviewResponse getEvaluationById(Long id);
    List<ReviewResponse> getEvaluationsByEmployeeId(Long employeeId);
    List<ReviewResponse> getEvaluationsByReviewPeriod(String reviewPeriod);
    List<ReviewResponse> getAllEvaluations();
}