package com.grading.service.impl;

import com.grading.dto.request.PromotionRequestRequest;
import com.grading.dto.response.PromotionRequestResponse;
import com.grading.entity.Employee;
import com.grading.entity.Grade;
import com.grading.entity.GradeHistory;
import com.grading.entity.PromotionRequest;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.GradeRepository;
import com.grading.repository.GradeHistoryRepository;
import com.grading.repository.PromotionRequestRepository;
import com.grading.service.PromotionRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionRequestServiceImpl implements PromotionRequestService {
    private final PromotionRequestRepository promotionRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final GradeRepository gradeRepository;
    private final GradeHistoryRepository gradeHistoryRepository;

    @Override
    @Transactional
    public PromotionRequestResponse createPromotionRequest(PromotionRequestRequest request, Long submittedById) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        Grade requestedGrade = gradeRepository.findById(request.getRequestedGradeId())
            .orElseThrow(() -> new RuntimeException("Grade not found"));
        Employee submittedBy = employeeRepository.findById(submittedById)
            .orElseThrow(() -> new RuntimeException("Submitter not found"));

        List<String> activeStatuses = Arrays.asList("pending", "under_review", "ready_for_calibration", "in_calibration");
        if (promotionRequestRepository.existsActiveRequest(request.getEmployeeId(), request.getRequestedGradeId(), activeStatuses)) {
            throw new RuntimeException("Active promotion request already exists for this employee and grade");
        }

        PromotionRequest promotionRequest = new PromotionRequest();
        promotionRequest.setEmployee(employee);
        promotionRequest.setRequestedGrade(requestedGrade);
        promotionRequest.setSubmittedBy(submittedBy);
        promotionRequest.setStatusChangedBy(submittedBy);
        promotionRequest.setCalibration(null);
        promotionRequest.setJustification(request.getJustification());
        promotionRequest.setEvidence(request.getEvidence());
        promotionRequest.setReviewPeriod(request.getReviewPeriod());
        promotionRequest.setStatus("pending");

        PromotionRequest saved = promotionRequestRepository.save(promotionRequest);
        return toPromotionRequestResponse(saved);
    }

    @Override
    @Transactional
    public PromotionRequestResponse updatePromotionRequest(Long id, PromotionRequestRequest request) {
        PromotionRequest promotionRequest = promotionRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion request not found"));
        
        Grade requestedGrade = gradeRepository.findById(request.getRequestedGradeId())
            .orElseThrow(() -> new RuntimeException("Grade not found"));
        
        promotionRequest.setRequestedGrade(requestedGrade);
        promotionRequest.setJustification(request.getJustification());
        promotionRequest.setEvidence(request.getEvidence());
        promotionRequest.setReviewPeriod(request.getReviewPeriod());
        
        if ("returned_for_revision".equalsIgnoreCase(promotionRequest.getStatus())) {
            promotionRequest.setStatus("pending");
            promotionRequest.setHrComment(null);
        }
        
        PromotionRequest saved = promotionRequestRepository.save(promotionRequest);
        return toPromotionRequestResponse(saved);
    }

    @Override
    @Transactional
    public PromotionRequestResponse updatePromotionRequestStatus(Long id, String status, Long changedById, String comment) {
        PromotionRequest promotionRequest = promotionRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion request not found"));
        Employee changedBy = employeeRepository.findById(changedById)
            .orElseThrow(() -> new RuntimeException("User not found"));

        promotionRequest.setStatus(status);
        promotionRequest.setStatusChangedBy(changedBy);
        
        if ("returned_for_revision".equalsIgnoreCase(status) && comment != null) {
            promotionRequest.setHrComment(comment);
        }

        PromotionRequest saved = promotionRequestRepository.save(promotionRequest);
        return toPromotionRequestResponse(saved);
    }

    @Override
    @Transactional
    public PromotionRequestResponse approveOrRejectPromotion(Long id, String decision, String comment, Long approvedById) {
        PromotionRequest promotionRequest = promotionRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion request not found"));
        
        Employee approvedBy = employeeRepository.findById(approvedById)
            .orElseThrow(() -> new RuntimeException("Approver not found"));

        // Проверяем, что заявка находится в статусе calibration_completed
        if (!"calibration_completed".equalsIgnoreCase(promotionRequest.getStatus())) {
            throw new RuntimeException("Promotion request must be in 'calibration_completed' status to approve or reject");
        }

        if ("approved".equalsIgnoreCase(decision)) {
            // Одобряем повышение
            promotionRequest.setStatus("approved");
            promotionRequest.setStatusChangedBy(approvedBy);
            promotionRequest.setHrComment(comment);
            
            // Создаем запись в grade_history
            GradeHistory currentGradeHistory = gradeHistoryRepository.findTopByEmployeeIdOrderByChangedAtDesc(
                promotionRequest.getEmployee().getId()
            ).orElse(null);
            
            GradeHistory gradeHistory = new GradeHistory();
            gradeHistory.setEmployee(promotionRequest.getEmployee());
            gradeHistory.setOldGrade(currentGradeHistory != null ? currentGradeHistory.getNewGrade() : null);
            gradeHistory.setNewGrade(promotionRequest.getRequestedGrade());
            gradeHistory.setChangedBy(approvedBy);
            gradeHistory.setReason("Promotion request approved: " + id);
            
            gradeHistoryRepository.save(gradeHistory);
            
        } else if ("rejected".equalsIgnoreCase(decision)) {
            // Отклоняем повышение
            promotionRequest.setStatus("rejected");
            promotionRequest.setStatusChangedBy(approvedBy);
            promotionRequest.setHrComment(comment != null ? comment : "Rejected");
        } else {
            throw new RuntimeException("Invalid decision. Must be 'approved' or 'rejected'");
        }

        PromotionRequest saved = promotionRequestRepository.save(promotionRequest);
        return toPromotionRequestResponse(saved);
    }

    @Override
    @Transactional
    public void deletePromotionRequest(Long id) {
        if (!promotionRequestRepository.existsById(id)) {
            throw new RuntimeException("Promotion request not found");
        }
        promotionRequestRepository.deleteById(id);
    }

    @Override
    public PromotionRequestResponse getPromotionRequestById(Long id) {
        PromotionRequest promotionRequest = promotionRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion request not found"));
        return toPromotionRequestResponse(promotionRequest);
    }

    @Override
    public List<PromotionRequestResponse> getPromotionRequestsByEmployeeId(Long employeeId) {
        return promotionRequestRepository.findByEmployeeId(employeeId).stream()
            .map(this::toPromotionRequestResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<PromotionRequestResponse> getPromotionRequestsByStatus(String status) {
        return promotionRequestRepository.findByStatus(status).stream()
            .map(this::toPromotionRequestResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<PromotionRequestResponse> getAllPromotionRequests() {
        return promotionRequestRepository.findAll().stream()
            .map(this::toPromotionRequestResponse)
            .collect(Collectors.toList());
    }

    private PromotionRequestResponse toPromotionRequestResponse(PromotionRequest pr) {
        PromotionRequestResponse response = new PromotionRequestResponse();
        response.setId(pr.getId());
        response.setEmployeeId(pr.getEmployee().getId());
        response.setEmployeeName(pr.getEmployee().getFullName());
        response.setRequestedGradeId(pr.getRequestedGrade().getId());
        response.setRequestedGradeName(pr.getRequestedGrade().getName());
        response.setSubmittedById(pr.getSubmittedBy().getId());
        response.setSubmittedByName(pr.getSubmittedBy().getFullName());
        response.setJustification(pr.getJustification());
        response.setEvidence(pr.getEvidence());
        response.setReviewPeriod(pr.getReviewPeriod());
        response.setStatus(pr.getStatus());
        response.setHrComment(pr.getHrComment());
        response.setCreatedAt(pr.getCreatedAt());
        return response;
    }
}