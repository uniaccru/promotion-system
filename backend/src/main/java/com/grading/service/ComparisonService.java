package com.grading.service;

import com.grading.dto.request.CreateComparisonRequest;
import com.grading.dto.response.ComparisonResponse;

import java.util.List;

public interface ComparisonService {
    ComparisonResponse createComparison(CreateComparisonRequest request, Long decidedById);
    List<ComparisonResponse> getComparisonsByCalibrationId(Long calibrationId);
    List<ComparisonResponse> getPendingComparisonsForEvaluator(Long calibrationId, Long evaluatorId);
}



