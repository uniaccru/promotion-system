package com.grading.service;

import com.grading.dto.request.PromotionRequestRequest;
import com.grading.dto.response.PromotionRequestResponse;

import java.util.List;

public interface PromotionRequestService {
    PromotionRequestResponse createPromotionRequest(PromotionRequestRequest request, Long submittedById);
    PromotionRequestResponse updatePromotionRequest(Long id, PromotionRequestRequest request);
    PromotionRequestResponse updatePromotionRequestStatus(Long id, String status, Long changedById, String comment);
    void deletePromotionRequest(Long id);
    PromotionRequestResponse getPromotionRequestById(Long id);
    List<PromotionRequestResponse> getPromotionRequestsByEmployeeId(Long employeeId);
    List<PromotionRequestResponse> getPromotionRequestsByStatus(String status);
    List<PromotionRequestResponse> getAllPromotionRequests();
}