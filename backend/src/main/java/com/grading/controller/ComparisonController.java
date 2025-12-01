package com.grading.controller;

import com.grading.dto.request.CreateComparisonRequest;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.ComparisonResponse;
import com.grading.entity.Employee;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.UserRepository;
import com.grading.service.ComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comparisons")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Comparisons", description = "Управление попарными сравнениями кандидатов")
public class ComparisonController {
    private final ComparisonService comparisonService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
        summary = "Создать сравнение",
        description = "Создает попарное сравнение двух кандидатов с выбором победителя (только evaluator)"
    )
    public ResponseEntity<ApiResponse<ComparisonResponse>> createComparison(
            @Valid @RequestBody CreateComparisonRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        String role = currentEmployee.getRole();
        if (!"team_lead".equalsIgnoreCase(role)) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only team leads can create comparisons"));
        }
        
        ComparisonResponse comparison = comparisonService.createComparison(request, currentEmployee.getId());
        return ResponseEntity.ok(ApiResponse.success("Comparison created successfully", comparison));
    }

    @GetMapping("/calibration/{calibrationId}")
    @Operation(
        summary = "Получить сравнения по калибровке",
        description = "Возвращает все попарные сравнения для указанной калибровки"
    )
    public ResponseEntity<ApiResponse<List<ComparisonResponse>>> getComparisonsByCalibrationId(
            @PathVariable Long calibrationId) {
        List<ComparisonResponse> comparisons = comparisonService.getComparisonsByCalibrationId(calibrationId);
        return ResponseEntity.ok(ApiResponse.success(comparisons));
    }

    @GetMapping("/calibration/{calibrationId}/pending")
    @Operation(
        summary = "Получить ожидающие сравнения",
        description = "Возвращает список пар кандидатов, которые еще не были сравнены"
    )
    public ResponseEntity<ApiResponse<List<ComparisonResponse>>> getPendingComparisons(
            @PathVariable Long calibrationId,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        List<ComparisonResponse> pending = comparisonService.getPendingComparisonsForEvaluator(calibrationId, currentEmployee.getId());
        return ResponseEntity.ok(ApiResponse.success(pending));
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


