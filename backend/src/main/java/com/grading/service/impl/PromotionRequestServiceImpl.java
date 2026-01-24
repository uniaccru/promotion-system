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
import com.grading.exception.BusinessLogicException;
import com.grading.exception.ResourceNotFoundException;
import com.grading.exception.ValidationException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionRequestServiceImpl implements PromotionRequestService {
    private static final Logger logger = LoggerFactory.getLogger(PromotionRequestServiceImpl.class);
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "image/jpeg",
        "image/png",
        "text/plain"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

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
            .orElseThrow(() -> new ResourceNotFoundException("Employee", request.getEmployeeId()));
        Grade requestedGrade = gradeRepository.findById(request.getRequestedGradeId())
            .orElseThrow(() -> new ResourceNotFoundException("Grade", request.getRequestedGradeId()));
        Employee submittedBy = employeeRepository.findById(submittedById)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", submittedById));

        List<String> activeStatuses = Arrays.asList("pending", "under_review", "ready_for_calibration", "in_calibration");
        if (promotionRequestRepository.existsActiveRequest(request.getEmployeeId(), request.getRequestedGradeId(), activeStatuses)) {
            throw new BusinessLogicException("Active promotion request already exists for this employee and grade");
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
            .orElseThrow(() -> new ResourceNotFoundException("Promotion request", id));
        
        Grade requestedGrade = gradeRepository.findById(request.getRequestedGradeId())
            .orElseThrow(() -> new ResourceNotFoundException("Grade", request.getRequestedGradeId()));
        
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
            .orElseThrow(() -> new ResourceNotFoundException("Promotion request", id));
        Employee changedBy = employeeRepository.findById(changedById)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", changedById));

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
            .orElseThrow(() -> new ResourceNotFoundException("Promotion request", id));
        
        Employee approvedBy = employeeRepository.findById(approvedById)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", approvedById));

        // Проверяем, что заявка находится в статусе calibration_completed
        if (!"calibration_completed".equalsIgnoreCase(promotionRequest.getStatus())) {
            throw new BusinessLogicException("Promotion request must be in 'calibration_completed' status to approve or reject");
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
            throw new ValidationException("Invalid decision. Must be 'approved' or 'rejected'");
        }

        PromotionRequest saved = promotionRequestRepository.save(promotionRequest);
        return toPromotionRequestResponse(saved);
    }

    @Override
    @Transactional
    public void deletePromotionRequest(Long id) {
        if (!promotionRequestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promotion request", id);
        }
        promotionRequestRepository.deleteById(id);
    }

    @Override
    public PromotionRequestResponse getPromotionRequestById(Long id) {
        PromotionRequest promotionRequest = promotionRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion request", id));
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
            .orElseThrow(() -> new ResourceNotFoundException("Promotion request", promotionRequestId));

        for (Long goalAssignmentId : goalAssignmentIds) {
            GoalAssignment goalAssignment = goalAssignmentRepository.findById(goalAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal assignment", goalAssignmentId));

            // Verify that the goal assignment belongs to the same employee
            if (!goalAssignment.getEmployee().getId().equals(promotionRequest.getEmployee().getId())) {
                throw new BusinessLogicException("Goal assignment does not belong to the employee");
            }

            // Verify that the goal is completed
            if (!"completed".equalsIgnoreCase(goalAssignment.getStatus())) {
                throw new BusinessLogicException("Only completed goals can be attached to promotion requests");
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
            .orElseThrow(() -> new ResourceNotFoundException("Promotion request", promotionRequestId));

        // Validate file
        validateFile(file);

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new ValidationException("File name cannot be empty");
            }

            // Sanitize filename to prevent path traversal
            String sanitizedFilename = sanitizeFilename(originalFilename);
            String extension = sanitizedFilename.contains(".") 
                ? sanitizedFilename.substring(sanitizedFilename.lastIndexOf(".")) 
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(uniqueFilename).normalize();

            // Additional security check: ensure the resolved path is still within upload directory
            if (!filePath.startsWith(uploadPath)) {
                throw new ValidationException("Invalid file path");
            }

            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save file info to database
            PromotionRequestFile fileEntity = new PromotionRequestFile();
            fileEntity.setPromotionRequest(promotionRequest);
            fileEntity.setFileName(sanitizedFilename);
            fileEntity.setFilePath(uniqueFilename);
            fileEntity.setFileSize(file.getSize());
            fileEntity.setContentType(file.getContentType());
            promotionRequestFileRepository.save(fileEntity);

            logger.info("File uploaded successfully: {} for promotion request {}", uniqueFilename, promotionRequestId);
            return toFileResponse(fileEntity);
        } catch (IOException e) {
            logger.error("Failed to upload file for promotion request {}", promotionRequestId, e);
            throw new BusinessLogicException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ValidationException("File type not allowed. Allowed types: PDF, DOC, DOCX, JPEG, PNG, TXT");
        }
    }

    private String sanitizeFilename(String filename) {
        // Remove path traversal attempts and dangerous characters
        return filename.replaceAll("[\\.]{2,}", "")
                      .replaceAll("[\\\\/:*?\"<>|]", "_")
                      .trim();
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
            .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(file.getFilePath()).normalize();
            
            // Security check: ensure the resolved path is still within upload directory
            if (!filePath.startsWith(uploadPath)) {
                throw new ValidationException("Invalid file path");
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or not readable: " + file.getFileName());
            }
        } catch (MalformedURLException e) {
            logger.error("Failed to download file {}", fileId, e);
            throw new BusinessLogicException("File not found: " + file.getFileName(), e);
        }
    }

    @Override
    public PromotionRequestFile getFileById(Long fileId) {
        return promotionRequestFileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File", fileId));
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        PromotionRequestFile file = promotionRequestFileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(file.getFilePath()).normalize();
            
            // Security check: ensure the resolved path is still within upload directory
            if (!filePath.startsWith(uploadPath)) {
                throw new ValidationException("Invalid file path");
            }
            
            Files.deleteIfExists(filePath);
            promotionRequestFileRepository.deleteById(fileId);
            logger.info("File deleted successfully: {}", fileId);
        } catch (IOException e) {
            logger.error("Failed to delete file {}", fileId, e);
            throw new BusinessLogicException("Failed to delete file: " + e.getMessage(), e);
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