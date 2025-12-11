package com.grading.service.impl;

import com.grading.dto.request.PromotionRequestRequest;
import com.grading.dto.response.PromotionRequestResponse;
import com.grading.dto.response.PromotionRequestFileResponse;
import com.grading.entity.Employee;
import com.grading.entity.Grade;
import com.grading.entity.GradeHistory;
import com.grading.entity.PromotionRequest;
import com.grading.entity.PromotionRequestFile;
import com.grading.entity.GoalAssignment;
import com.grading.model.PromotionRequestGoal;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.GradeRepository;
import com.grading.repository.GradeHistoryRepository;
import com.grading.repository.PromotionRequestRepository;
import com.grading.repository.GoalAssignmentRepository;
import com.grading.repository.PromotionRequestGoalRepository;
import com.grading.repository.PromotionRequestFileRepository;
import com.grading.service.PromotionRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionRequestServiceImpl implements PromotionRequestService {
    private final PromotionRequestRepository promotionRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final GradeRepository gradeRepository;
    private final GradeHistoryRepository gradeHistoryRepository;
    private final GoalAssignmentRepository goalAssignmentRepository;
    private final PromotionRequestGoalRepository promotionRequestGoalRepository;
    private final PromotionRequestFileRepository promotionRequestFileRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

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

    @Override
    @Transactional
    public void attachGoalsToPromotionRequest(Long promotionRequestId, List<Long> goalAssignmentIds) {
        PromotionRequest promotionRequest = promotionRequestRepository.findById(promotionRequestId)
            .orElseThrow(() -> new RuntimeException("Promotion request not found"));

        for (Long goalAssignmentId : goalAssignmentIds) {
            GoalAssignment goalAssignment = goalAssignmentRepository.findById(goalAssignmentId)
                .orElseThrow(() -> new RuntimeException("Goal assignment not found: " + goalAssignmentId));

            // Verify that the goal assignment belongs to the same employee
            if (!goalAssignment.getEmployee().getId().equals(promotionRequest.getEmployee().getId())) {
                throw new RuntimeException("Goal assignment does not belong to the employee");
            }

            // Verify that the goal is completed
            if (!"completed".equalsIgnoreCase(goalAssignment.getStatus())) {
                throw new RuntimeException("Only completed goals can be attached to promotion requests");
            }

            PromotionRequestGoal prGoal = new PromotionRequestGoal();
            prGoal.setPromotionRequestId(promotionRequestId);
            prGoal.setGoalAssignmentId(goalAssignmentId);
            promotionRequestGoalRepository.save(prGoal);
        }
    }

    @Override
    @Transactional
    public void detachGoalFromPromotionRequest(Long promotionRequestId, Long goalAssignmentId) {
        PromotionRequestGoal.PromotionRequestGoalId id = 
            new PromotionRequestGoal.PromotionRequestGoalId(promotionRequestId, goalAssignmentId);
        promotionRequestGoalRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PromotionRequestFileResponse uploadFile(Long promotionRequestId, MultipartFile file) {
        PromotionRequest promotionRequest = promotionRequestRepository.findById(promotionRequestId)
            .orElseThrow(() -> new RuntimeException("Promotion request not found"));

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save file info to database
            PromotionRequestFile fileEntity = new PromotionRequestFile();
            fileEntity.setPromotionRequest(promotionRequest);
            fileEntity.setFileName(originalFilename);
            fileEntity.setFilePath(uniqueFilename);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setContentType(file.getContentType());
            promotionRequestFileRepository.save(fileEntity);

            return toFileResponse(fileEntity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PromotionRequestFileResponse> getPromotionRequestFiles(Long promotionRequestId) {
        List<PromotionRequestFile> files = promotionRequestFileRepository.findByPromotionRequestId(promotionRequestId);
        return files.stream()
            .map(this::toFileResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Resource downloadFile(Long fileId) {
        PromotionRequestFile file = promotionRequestFileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            Path filePath = Paths.get(uploadDir).resolve(file.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + file.getFileName());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + file.getFileName(), e);
        }
    }

    @Override
    public PromotionRequestFile getFileById(Long fileId) {
        return promotionRequestFileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        PromotionRequestFile file = promotionRequestFileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            Path filePath = Paths.get(uploadDir).resolve(file.getFilePath()).normalize();
            Files.deleteIfExists(filePath);
            promotionRequestFileRepository.deleteById(fileId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    private PromotionRequestFileResponse toFileResponse(PromotionRequestFile file) {
        PromotionRequestFileResponse response = new PromotionRequestFileResponse();
        response.setId(file.getId());
        response.setPromotionRequestId(file.getPromotionRequest().getId());
        response.setFileName(file.getFileName());
        response.setFileSize(file.getFileSize());
        response.setContentType(file.getContentType());
        response.setUploadedAt(file.getUploadedAt());
        return response;
    }
}