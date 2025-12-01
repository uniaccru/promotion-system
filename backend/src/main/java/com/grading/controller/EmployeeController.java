package com.grading.controller;

import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.EmployeeProfileResponse;
import com.grading.dto.response.EmployeeResponse;
import com.grading.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Employees", description = "Управление сотрудниками")
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping("/{id}/profile")
    @Operation(
        summary = "Получить профиль сотрудника",
        description = "Возвращает полный профиль сотрудника с текущим грейдом, последней оценкой и историей изменений"
    )
    public ResponseEntity<ApiResponse<EmployeeProfileResponse>> getEmployeeProfile(@PathVariable Long id) {
        EmployeeProfileResponse profile = employeeService.getEmployeeProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping
    @Operation(
        summary = "Получить всех сотрудников",
        description = "Возвращает список всех сотрудников в системе"
    )
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить сотрудника по ID",
        description = "Возвращает информацию о сотруднике по его идентификатору"
    )
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @GetMapping("/department/{department}")
    @Operation(
        summary = "Получить сотрудников по отделу",
        description = "Возвращает список сотрудников указанного отдела"
    )
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByDepartment(@PathVariable String department) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success(employees));
    }
}