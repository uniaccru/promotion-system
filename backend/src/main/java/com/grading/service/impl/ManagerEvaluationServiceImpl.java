package com.grading.service.impl;

import com.grading.dto.request.ManagerEvaluationRequest;
import com.grading.dto.response.ReviewResponse;
import com.grading.entity.Employee;
import com.grading.entity.ManagerEvaluation;
import com.grading.exception.ResourceNotFoundException;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.ManagerEvaluationRepository;
import com.grading.service.ManagerEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerEvaluationServiceImpl implements ManagerEvaluationService {
    private final ManagerEvaluationRepository managerEvaluationRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public ReviewResponse createEvaluation(ManagerEvaluationRequest request, Long evaluatorId) {
        Employee evaluator = employeeRepository.findById(evaluatorId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", evaluatorId));
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new ResourceNotFoundException("Employee", request.getEmployeeId()));

        ManagerEvaluation evaluation = new ManagerEvaluation();
        evaluation.setEvaluator(evaluator);
        evaluation.setEmployee(employee);
        evaluation.setReviewPeriod(request.getReviewPeriod());
        evaluation.setScore(request.getScore());
        evaluation.setComment(request.getComment());
        evaluation.setNominatedForPromotion(request.getNominatedForPromotion());

        ManagerEvaluation saved = managerEvaluationRepository.save(evaluation);
        return toReviewResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse updateEvaluation(Long id, ManagerEvaluationRequest request) {
        ManagerEvaluation evaluation = managerEvaluationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Evaluation not found"));

        evaluation.setReviewPeriod(request.getReviewPeriod());
        evaluation.setScore(request.getScore());
        evaluation.setComment(request.getComment());
        evaluation.setNominatedForPromotion(request.getNominatedForPromotion());

        ManagerEvaluation saved = managerEvaluationRepository.save(evaluation);
        return toReviewResponse(saved);
    }

    @Override
    public ReviewResponse getEvaluationById(Long id) {
        ManagerEvaluation evaluation = managerEvaluationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Evaluation", id));
        return toReviewResponse(evaluation);
    }

    @Override
    public List<ReviewResponse> getEvaluationsByEmployeeId(Long employeeId) {
        return managerEvaluationRepository.findByEmployeeId(employeeId).stream()
            .map(this::toReviewResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getEvaluationsByReviewPeriod(String reviewPeriod) {
        return managerEvaluationRepository.findByReviewPeriod(reviewPeriod).stream()
            .map(this::toReviewResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponse> getAllEvaluations() {
        return managerEvaluationRepository.findAll().stream()
            .map(this::toReviewResponse)
            .collect(Collectors.toList());
    }

    private ReviewResponse toReviewResponse(ManagerEvaluation evaluation) {
        ReviewResponse response = new ReviewResponse();
        response.setId(evaluation.getId());
        response.setEvaluatorId(evaluation.getEvaluator().getId());
        response.setEvaluatorName(evaluation.getEvaluator().getFullName());
        response.setEmployeeId(evaluation.getEmployee().getId());
        response.setEmployeeName(evaluation.getEmployee().getFullName());
        response.setReviewPeriod(evaluation.getReviewPeriod());
        response.setScore(evaluation.getScore());
        response.setComment(evaluation.getComment());
        response.setNominatedForPromotion(evaluation.getNominatedForPromotion());
        response.setCreatedAt(evaluation.getCreatedAt());
        response.setUpdatedAt(evaluation.getUpdatedAt());
        return response;
    }
}