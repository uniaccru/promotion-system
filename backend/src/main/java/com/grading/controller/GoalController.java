package com.grading.controller;

import com.grading.dto.request.AssignGoalRequest;
import com.grading.dto.request.UpdateGoalStatusRequest;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.GoalResponse;
import com.grading.entity.Employee;
import com.grading.util.SecurityUtils;
import com.grading.service.GoalService;
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
@RequestMapping("/goals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Goals", description = "Управление назначенными целями сотрудников")
public class GoalController {
    private final GoalService goalService;
    private final SecurityUtils securityUtils;

    @PostMapping("/assign")
    @Operation(
        summary = "Назначить цель сотруднику",
        description = "HR назначает общую цель конкретному сотруднику на текущий квартал"
    )
    public ResponseEntity<ApiResponse<GoalResponse>> assignGoal(
            @Valid @RequestBody AssignGoalRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can assign goals"));
        }
        
        GoalResponse goal = goalService.assignGoal(request);
        return ResponseEntity.ok(ApiResponse.success("Goal assigned successfully", goal));
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Обновить статус цели",
        description = "Сотрудник может изменить статус своей назначенной цели"
    )
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoalStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGoalStatusRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        GoalResponse goalAssignment = goalService.getGoalAssignmentById(id);
        if (!goalAssignment.getEmployeeId().equals(currentEmployee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only update your own goals"));
        }
        
        GoalResponse updated = goalService.updateGoalStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Goal status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить назначение цели",
        description = "HR может удалить назначение цели сотруднику"
    )
    public ResponseEntity<ApiResponse<Void>> deleteGoalAssignment(
            @PathVariable Long id,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can delete goal assignments"));
        }
        
        goalService.deleteGoalAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Goal assignment deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить назначение цели по ID",
        description = "Возвращает информацию о назначенной цели по идентификатору"
    )
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalAssignmentById(@PathVariable Long id) {
        GoalResponse goal = goalService.getGoalAssignmentById(id);
        return ResponseEntity.ok(ApiResponse.success(goal));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(
        summary = "Получить цели сотрудника",
        description = "Возвращает все назначенные цели конкретного сотрудника"
    )
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsByEmployeeId(
            @PathVariable Long employeeId,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!currentEmployee.getId().equals(employeeId) && !"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You can only view your own goals"));
        }
        
        List<GoalResponse> goals = goalService.getGoalsByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/my-goals")
    @Operation(
        summary = "Получить свои цели",
        description = "Возвращает все назначенные цели текущего сотрудника"
    )
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getMyGoals(Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        List<GoalResponse> goals = goalService.getGoalsByEmployeeId(currentEmployee.getId());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/my-goals/completed")
    @Operation(
        summary = "Получить свои выполненные цели",
        description = "Возвращает все выполненные цели текущего сотрудника для прикрепления к заявке на повышение"
    )
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getMyCompletedGoals(Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        List<GoalResponse> goals = goalService.getGoalsByEmployeeIdAndStatus(currentEmployee.getId(), "completed");
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    @GetMapping("/all")
    @Operation(
        summary = "Получить все назначения целей",
        description = "HR может получить все назначения целей всем сотрудникам"
    )
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getAllGoalAssignments(Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can view all goal assignments"));
        }
        
        List<GoalResponse> goals = goalService.getAllGoalAssignments();
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    private Employee getCurrentEmployee(Authentication authentication) {
        return securityUtils.getCurrentEmployee(authentication);
    }
}