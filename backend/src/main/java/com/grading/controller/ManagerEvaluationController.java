package com.grading.controller;

import com.grading.dto.request.ManagerEvaluationRequest;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.ReviewResponse;
import com.grading.entity.Employee;
import com.grading.util.SecurityUtils;
import com.grading.service.ManagerEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Manager Evaluations", description = "Управление оценками менеджера")
public class ManagerEvaluationController {
    private final ManagerEvaluationService managerEvaluationService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(
        summary = "Создать оценку",
        description = "HR создает оценку для сотрудника с баллом, комментарием и номинацией на повышение"
    )
    public ResponseEntity<ApiResponse<ReviewResponse>> createEvaluation(
            @Valid @RequestBody ManagerEvaluationRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can create reviews"));
        }
        
        ReviewResponse evaluation = managerEvaluationService.createEvaluation(request, currentEmployee.getId());
        return ResponseEntity.ok(ApiResponse.success("Review created successfully", evaluation));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить оценку",
        description = "Обновляет существующую оценку сотрудника (только HR)"
    )
    public ResponseEntity<ApiResponse<ReviewResponse>> updateEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody ManagerEvaluationRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can update reviews"));
        }
        
        ReviewResponse evaluation = managerEvaluationService.updateEvaluation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", evaluation));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить оценку по ID",
        description = "Возвращает оценку по идентификатору"
    )
    public ResponseEntity<ApiResponse<ReviewResponse>> getEvaluationById(
            @PathVariable Long id,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        ReviewResponse evaluation = managerEvaluationService.getEvaluationById(id);
        
        if ("employee".equalsIgnoreCase(currentEmployee.getRole()) && 
            !evaluation.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only view your own reviews"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(evaluation));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(
        summary = "Получить оценки сотрудника",
        description = "Возвращает все оценки конкретного сотрудника"
    )
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getEvaluationsByEmployeeId(
            @PathVariable Long employeeId,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if ("employee".equalsIgnoreCase(currentEmployee.getRole()) && 
            !employeeId.equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only view your own reviews"));
        }
        
        List<ReviewResponse> evaluations = managerEvaluationService.getEvaluationsByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(evaluations));
    }

    @GetMapping
    @Operation(
        summary = "Получить все оценки",
        description = "Возвращает все оценки всех сотрудников (только HR)"
    )
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAllEvaluations(
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can view all reviews"));
        }
        
        List<ReviewResponse> evaluations = managerEvaluationService.getAllEvaluations();
        return ResponseEntity.ok(ApiResponse.success(evaluations));
    }

    @GetMapping("/review-period/{reviewPeriod}")
    @Operation(
        summary = "Получить оценки по периоду ревью",
        description = "Возвращает все оценки для указанного периода ревью (только HR)"
    )
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getEvaluationsByReviewPeriod(
            @PathVariable String reviewPeriod,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can view reviews by period"));
        }
        
        List<ReviewResponse> evaluations = managerEvaluationService.getEvaluationsByReviewPeriod(reviewPeriod);
        return ResponseEntity.ok(ApiResponse.success(evaluations));
    }

    private Employee getCurrentEmployee(Authentication authentication) {
        return securityUtils.getCurrentEmployee(authentication);
    }
}