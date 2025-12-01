package com.grading.controller;

import com.grading.dto.request.CreateCalibrationPackageRequest;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.CalibrationResponse;
import com.grading.dto.response.CandidateRankingResponse;
import com.grading.entity.Calibration;
import com.grading.entity.Employee;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.UserRepository;
import com.grading.service.CalibrationService;
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
@RequestMapping("/calibrations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Calibration", description = "Управление калибровкой и попарным сравнением кандидатов")
public class CalibrationController {
    private final CalibrationService calibrationService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
        summary = "Создать калибровку",
        description = "Создает новый пакет калибровки для указанного грейда"
    )
    public ResponseEntity<ApiResponse<Calibration>> createCalibration(
            @RequestParam Long gradeId,
            Authentication authentication) {
        Long createdById = 1L;
        Calibration calibration = calibrationService.createCalibration(gradeId, createdById);
        return ResponseEntity.ok(ApiResponse.success("Calibration created successfully", calibration));
    }

    @PutMapping("/{id}/status")
    @Operation(
        summary = "Обновить статус калибровки",
        description = "Изменяет статус калибровки (planning, active, completed)"
    )
    public ResponseEntity<ApiResponse<Calibration>> updateCalibrationStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Calibration calibration = calibrationService.updateCalibrationStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Calibration status updated successfully", calibration));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить калибровку по ID",
        description = "Возвращает информацию о калибровке по идентификатору"
    )
    public ResponseEntity<ApiResponse<Calibration>> getCalibrationById(@PathVariable Long id) {
        Calibration calibration = calibrationService.getCalibrationById(id);
        return ResponseEntity.ok(ApiResponse.success(calibration));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Получить калибровки по статусу",
        description = "Возвращает все калибровки с указанным статусом"
    )
    public ResponseEntity<ApiResponse<List<CalibrationResponse>>> getCalibrationsByStatus(@PathVariable String status) {
        List<CalibrationResponse> calibrations = calibrationService.getCalibrationResponsesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(calibrations));
    }

    @GetMapping("/grade/{gradeId}")
    @Operation(
        summary = "Получить калибровки по грейду",
        description = "Возвращает все калибровки для указанного грейда"
    )
    public ResponseEntity<ApiResponse<List<Calibration>>> getCalibrationsByGradeId(@PathVariable Long gradeId) {
        List<Calibration> calibrations = calibrationService.getCalibrationsByGradeId(gradeId);
        return ResponseEntity.ok(ApiResponse.success(calibrations));
    }

    @PostMapping("/package")
    @Operation(
        summary = "Создать пакет калибровки",
        description = "Создает пакет калибровки из promotion requests и назначает evaluators (только HR)"
    )
    public ResponseEntity<ApiResponse<CalibrationResponse>> createCalibrationPackage(
            @Valid @RequestBody CreateCalibrationPackageRequest request,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only HR can create calibration packages"));
        }
        
        CalibrationResponse calibration = calibrationService.createCalibrationPackage(request, currentEmployee.getId());
        return ResponseEntity.ok(ApiResponse.success("Calibration package created successfully", calibration));
    }

    @GetMapping("/{id}/details")
    @Operation(
        summary = "Получить детали калибровки",
        description = "Возвращает полную информацию о калибровке с evaluators и candidates"
    )
    public ResponseEntity<ApiResponse<CalibrationResponse>> getCalibrationDetails(@PathVariable Long id) {
        CalibrationResponse calibration = calibrationService.getCalibrationResponseById(id);
        return ResponseEntity.ok(ApiResponse.success(calibration));
    }

    @GetMapping("/evaluator/{evaluatorId}")
    @Operation(
        summary = "Получить калибровки для evaluator",
        description = "Возвращает все калибровки, назначенные указанному evaluator"
    )
    public ResponseEntity<ApiResponse<List<CalibrationResponse>>> getCalibrationsByEvaluatorId(
            @PathVariable Long evaluatorId,
            Authentication authentication) {
        Employee currentEmployee = getCurrentEmployee(authentication);
        
        if (!currentEmployee.getId().equals(evaluatorId) && !"hr".equalsIgnoreCase(currentEmployee.getRole())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied"));
        }
        
        List<CalibrationResponse> calibrations = calibrationService.getCalibrationsByEvaluatorId(evaluatorId);
        return ResponseEntity.ok(ApiResponse.success(calibrations));
    }

    @GetMapping("/{id}/ranking")
    @Operation(
        summary = "Получить рейтинг кандидатов",
        description = "Возвращает рейтинг кандидатов на основе попарных сравнений"
    )
    public ResponseEntity<ApiResponse<CandidateRankingResponse>> getCandidateRanking(@PathVariable Long id) {
        CandidateRankingResponse ranking = calibrationService.getCandidateRanking(id);
        return ResponseEntity.ok(ApiResponse.success(ranking));
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