package com.grading.controller;

import com.grading.dto.request.CreateGoalRequest;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.GoalTemplateResponse;
import com.grading.entity.Employee;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.UserRepository;
import com.grading.service.GoalTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goal-templates")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Goal Templates", description = "Управление общими шаблонами целей")
public class GoalTemplateController {
    private final GoalTemplateService goalTemplateService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
        summary = "Создать общую цель",
        description = "HR создает общую цель, которая может быть назначена сотрудникам"
    )
    public ResponseEntity<ApiResponse<GoalTemplateResponse>> createGoalTemplate(
            @Valid @RequestBody CreateGoalRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can create goal templates"));
        }
        
        GoalTemplateResponse goal = goalTemplateService.createGoalTemplate(request);
        return ResponseEntity.ok(ApiResponse.success("Goal template created successfully", goal));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Обновить общую цель",
        description = "HR обновляет общую цель"
    )
    public ResponseEntity<ApiResponse<GoalTemplateResponse>> updateGoalTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CreateGoalRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can update goal templates"));
        }
        
        GoalTemplateResponse goal = goalTemplateService.updateGoalTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success("Goal template updated successfully", goal));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить общую цель",
        description = "HR удаляет общую цель"
    )
    public ResponseEntity<ApiResponse<Void>> deleteGoalTemplate(
            @PathVariable Long id,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can delete goal templates"));
        }
        
        goalTemplateService.deleteGoalTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Goal template deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить общую цель по ID",
        description = "Возвращает информацию об общей цели по идентификатору"
    )
    public ResponseEntity<ApiResponse<GoalTemplateResponse>> getGoalTemplateById(@PathVariable Long id) {
        GoalTemplateResponse goal = goalTemplateService.getGoalTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    @GetMapping
    @Operation(
        summary = "Получить все общие цели",
        description = "Возвращает все общие цели (доступно всем)"
    )
    public ResponseEntity<ApiResponse<List<GoalTemplateResponse>>> getAllGoalTemplates() {
        List<GoalTemplateResponse> goals = goalTemplateService.getAllGoalTemplates();
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/review-period/{reviewPeriod}")
    @Operation(
        summary = "Получить общие цели по периоду ревью",
        description = "Возвращает все общие цели для указанного периода ревью"
    )
    public ResponseEntity<ApiResponse<List<GoalTemplateResponse>>> getGoalTemplatesByReviewPeriod(
            @PathVariable String reviewPeriod) {
        List<GoalTemplateResponse> goals = goalTemplateService.getGoalTemplatesByReviewPeriod(reviewPeriod);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    private Employee getCurrentEmployee(Authentication authentication) {
        String username = authentication.getName();
        return employeeRepository.findByUserId(
            userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId()
        ).orElseThrow(() -> new RuntimeException("Employee not found"));
    }
}

