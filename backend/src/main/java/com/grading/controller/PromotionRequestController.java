package com.grading.controller;

import com.grading.dto.request.ApprovePromotionRequest;
import com.grading.dto.request.PromotionRequestRequest;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.GoalResponse;
import com.grading.dto.response.PromotionRequestResponse;
import com.grading.dto.response.PromotionRequestFileResponse;
import com.grading.entity.Employee;
import com.grading.entity.PromotionRequestFile;
import com.grading.model.PromotionRequestGoal;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.PromotionRequestGoalRepository;
import com.grading.repository.UserRepository;
import com.grading.service.PromotionRequestService;
import com.grading.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/promotion-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Promotion Requests", description = "Управление заявками на повышение")
public class PromotionRequestController {
    private final PromotionRequestService promotionRequestService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PromotionRequestGoalRepository promotionRequestGoalRepository;
    private final GoalService goalService;

    @PostMapping
    @Operation(
        summary = "Создать заявку на повышение",
        description = "Создает новую заявку на повышение грейда с обоснованием и доказательствами (employee и team_lead)"
    )
    public ResponseEntity<ApiResponse<PromotionRequestResponse>> createPromotionRequest(
            @Valid @RequestBody PromotionRequestRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        String role = currentEmployee.getRole().toLowerCase();
        if (!role.equals("employee") && !role.equals("team_lead")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only employees and team leads can create promotion requests"));
        }
        
        if (!request.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only create promotion requests for yourself"));
        }
        
        PromotionRequestResponse promotionRequest = promotionRequestService.createPromotionRequest(request, currentEmployee.getId());
        return ResponseEntity.ok(ApiResponse.success("Promotion request created successfully", promotionRequest));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить заявку",
        description = "Обновляет заявку на повышение (только для pending и returned_for_revision статуса, только автор)"
    )
    public ResponseEntity<ApiResponse<PromotionRequestResponse>> updatePromotionRequest(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequestRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        PromotionRequestResponse existing = promotionRequestService.getPromotionRequestById(id);
        
        if (!existing.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only update your own promotion requests"));
        }
        
        if (!"pending".equalsIgnoreCase(existing.getStatus()) && 
            !"returned_for_revision".equalsIgnoreCase(existing.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only update promotion requests in pending or returned_for_revision status"));
        }
        
        PromotionRequestResponse updated = promotionRequestService.updatePromotionRequest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Promotion request updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить заявку",
        description = "Удаляет заявку на повышение (только для pending и returned_for_revision статуса, только автор)"
    )
    public ResponseEntity<ApiResponse<Void>> deletePromotionRequest(
            @PathVariable Long id,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        PromotionRequestResponse existing = promotionRequestService.getPromotionRequestById(id);
        
        if (!existing.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only delete your own promotion requests"));
        }
        
        if (!"pending".equalsIgnoreCase(existing.getStatus()) && 
            !"returned_for_revision".equalsIgnoreCase(existing.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only delete promotion requests in pending or returned_for_revision status"));
        }
        
        promotionRequestService.deletePromotionRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Promotion request deleted successfully", null));
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Обновить статус заявки",
        description = "Изменяет статус заявки на повышение (pending, under_review, approved, rejected) - только HR"
    )
    public ResponseEntity<ApiResponse<PromotionRequestResponse>> updatePromotionRequestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comment,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can update promotion request status"));
        }
        
        if ("returned_for_revision".equalsIgnoreCase(status) && (comment == null || comment.trim().isEmpty())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Comment is required when returning for revision"));
        }
        
        PromotionRequestResponse promotionRequest = promotionRequestService.updatePromotionRequestStatus(id, status, currentEmployee.getId(), comment);
        return ResponseEntity.ok(ApiResponse.success("Promotion request status updated successfully", promotionRequest));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить заявку по ID",
        description = "Возвращает информацию о заявке на повышение по идентификатору"
    )
    public ResponseEntity<ApiResponse<PromotionRequestResponse>> getPromotionRequestById(
            @PathVariable Long id,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        PromotionRequestResponse promotionRequest = promotionRequestService.getPromotionRequestById(id);
        
        if ("employee".equalsIgnoreCase(currentEmployee.getRole()) && 
            !promotionRequest.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only view your own promotion requests"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(promotionRequest));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(
        summary = "Получить заявки сотрудника",
        description = "Возвращает все заявки на повышение конкретного сотрудника"
    )
    public ResponseEntity<ApiResponse<List<PromotionRequestResponse>>> getPromotionRequestsByEmployeeId(
            @PathVariable Long employeeId,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if ("employee".equalsIgnoreCase(currentEmployee.getRole()) && 
            !employeeId.equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only view your own promotion requests"));
        }
        
        List<PromotionRequestResponse> promotionRequests = promotionRequestService.getPromotionRequestsByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(promotionRequests));
    }

    @GetMapping
    @Operation(
        summary = "Получить все заявки",
        description = "Возвращает все заявки на повышение (только HR)"
    )
    public ResponseEntity<ApiResponse<List<PromotionRequestResponse>>> getAllPromotionRequests(
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can view all promotion requests"));
        }
        
        List<PromotionRequestResponse> promotionRequests = promotionRequestService.getAllPromotionRequests();
        return ResponseEntity.ok(ApiResponse.success(promotionRequests));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Получить заявки по статусу",
        description = "Возвращает все заявки с указанным статусом (только HR)"
    )
    public ResponseEntity<ApiResponse<List<PromotionRequestResponse>>> getPromotionRequestsByStatus(
            @PathVariable String status,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can filter promotion requests by status"));
        }
        
        List<PromotionRequestResponse> promotionRequests = promotionRequestService.getPromotionRequestsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(promotionRequests));
    }

    @PostMapping("/{id}/decision")
    @Operation(
        summary = "Одобрить или отклонить повышение",
        description = "Одобряет или отклоняет заявку на повышение после калибровки. При одобрении автоматически присваивается новый грейд (только HR)"
    )
    public ResponseEntity<ApiResponse<PromotionRequestResponse>> approveOrRejectPromotion(
            @PathVariable Long id,
            @Valid @RequestBody ApprovePromotionRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can approve or reject promotion requests"));
        }
        
        PromotionRequestResponse promotionRequest = promotionRequestService.approveOrRejectPromotion(
            id, 
            request.getDecision(), 
            request.getComment(), 
            currentEmployee.getId()
        );
        
        String message = "approved".equalsIgnoreCase(request.getDecision()) 
            ? "Promotion request approved successfully. New grade assigned." 
            : "Promotion request rejected successfully.";
        
        return ResponseEntity.ok(ApiResponse.success(message, promotionRequest));
    }

    @PostMapping("/{id}/goals")
    @Operation(
        summary = "Прикрепить цели к заявке",
        description = "Прикрепляет выполненные цели к заявке на повышение"
    )
    public ResponseEntity<ApiResponse<String>> attachGoalsToPromotionRequest(
            @PathVariable Long id,
            @RequestBody List<Long> goalAssignmentIds,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        // Verify that the employee owns this promotion request
        PromotionRequestResponse pr = promotionRequestService.getPromotionRequestById(id);
        if (!pr.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only attach goals to your own promotion requests"));
        }
        
        promotionRequestService.attachGoalsToPromotionRequest(id, goalAssignmentIds);
        return ResponseEntity.ok(ApiResponse.success("Goals attached successfully"));
    }

    @DeleteMapping("/{id}/goals/{goalAssignmentId}")
    @Operation(
        summary = "Открепить цель от заявки",
        description = "Открепляет цель от заявки на повышение"
    )
    public ResponseEntity<ApiResponse<String>> detachGoalFromPromotionRequest(
            @PathVariable Long id,
            @PathVariable Long goalAssignmentId,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        // Verify that the employee owns this promotion request
        PromotionRequestResponse pr = promotionRequestService.getPromotionRequestById(id);
        if (!pr.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only detach goals from your own promotion requests"));
        }
        
        promotionRequestService.detachGoalFromPromotionRequest(id, goalAssignmentId);
        return ResponseEntity.ok(ApiResponse.success("Goal detached successfully"));
    }

    @GetMapping("/{id}/goals")
    @Operation(
        summary = "Получить цели заявки",
        description = "Возвращает все прикрепленные цели к заявке на повышение"
    )
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getPromotionRequestGoals(@PathVariable Long id) {
        List<PromotionRequestGoal> prGoals = promotionRequestGoalRepository.findByPromotionRequestIdWithGoals(id);
        List<GoalResponse> goals = prGoals.stream()
            .map(prg -> goalService.getGoalAssignmentById(prg.getGoalAssignmentId()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @PostMapping("/{id}/files")
    @Operation(
        summary = "Загрузить файл к заявке",
        description = "Загружает файл-доказательство к заявке на повышение"
    )
    public ResponseEntity<ApiResponse<PromotionRequestFileResponse>> uploadFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        PromotionRequestResponse pr = promotionRequestService.getPromotionRequestById(id);
        if (!pr.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only upload files to your own promotion requests"));
        }
        
        PromotionRequestFileResponse fileResponse = promotionRequestService.uploadFile(id, file);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileResponse));
    }

    @GetMapping("/{id}/files")
    @Operation(
        summary = "Получить файлы заявки",
        description = "Возвращает все прикрепленные файлы к заявке на повышение"
    )
    public ResponseEntity<ApiResponse<List<PromotionRequestFileResponse>>> getPromotionRequestFiles(@PathVariable Long id) {
        List<PromotionRequestFileResponse> files = promotionRequestService.getPromotionRequestFiles(id);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    @GetMapping("/files/{fileId}/download")
    @Operation(
        summary = "Скачать файл",
        description = "Скачивает файл по его ID"
    )
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        // Get file metadata first
        PromotionRequestFile fileEntity = promotionRequestService.getFileById(fileId);
        
        // Get file resource
        Resource resource = promotionRequestService.downloadFile(fileId);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(fileEntity.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"")
            .body(resource);
    }

    @DeleteMapping("/files/{fileId}")
    @Operation(
        summary = "Удалить файл",
        description = "Удаляет файл из заявки на повышение"
    )
    public ResponseEntity<ApiResponse<String>> deleteFile(
            @PathVariable Long fileId,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        // TODO: Add permission check
        
        promotionRequestService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
    }

    private Employee getCurrentEmployee(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        return userRepository.findByUsername(username)
            .flatMap(user -> employeeRepository.findByUserId(user.getId()))
            .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));
    }
}