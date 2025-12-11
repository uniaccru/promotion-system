package com.grading.service;

import com.grading.dto.request.PromotionRequestRequest;
import com.grading.dto.response.PromotionRequestResponse;
import com.grading.dto.response.PromotionRequestFileResponse;
import com.grading.entity.PromotionRequestFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.util.List;

public interface PromotionRequestService {
    PromotionRequestResponse createPromotionRequest(PromotionRequestRequest request, Long submittedById);
    PromotionRequestResponse updatePromotionRequest(Long id, PromotionRequestRequest request);
    PromotionRequestResponse updatePromotionRequestStatus(Long id, String status, Long changedById, String comment);
    PromotionRequestResponse approveOrRejectPromotion(Long id, String decision, String comment, Long approvedById);
    void deletePromotionRequest(Long id);
    PromotionRequestResponse getPromotionRequestById(Long id);
    List<PromotionRequestResponse> getPromotionRequestsByEmployeeId(Long employeeId);
    List<PromotionRequestResponse> getPromotionRequestsByStatus(String status);
    List<PromotionRequestResponse> getAllPromotionRequests();
    void attachGoalsToPromotionRequest(Long promotionRequestId, List<Long> goalAssignmentIds);
    void detachGoalFromPromotionRequest(Long promotionRequestId, Long goalAssignmentId);
    PromotionRequestFileResponse uploadFile(Long promotionRequestId, MultipartFile file);
    List<PromotionRequestFileResponse> getPromotionRequestFiles(Long promotionRequestId);
    PromotionRequestFile getFileById(Long fileId);
    Resource downloadFile(Long fileId);
    void deleteFile(Long fileId);
}