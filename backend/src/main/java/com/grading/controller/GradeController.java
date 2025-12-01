package com.grading.controller;

import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.GradeResponse;
import com.grading.entity.Grade;
import com.grading.repository.GradeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/grades")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Grades", description = "Управление грейдами")
public class GradeController {
    private final GradeRepository gradeRepository;

    @GetMapping
    @Operation(
        summary = "Получить все грейды",
        description = "Возвращает список всех грейдов в системе"
    )
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getAllGrades() {
        List<GradeResponse> grades = gradeRepository.findAll().stream()
            .map(this::toGradeResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить грейд по ID",
        description = "Возвращает информацию о грейде по идентификатору"
    )
    public ResponseEntity<ApiResponse<GradeResponse>> getGradeById(@PathVariable Long id) {
        Grade grade = gradeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Grade not found"));
        return ResponseEntity.ok(ApiResponse.success(toGradeResponse(grade)));
    }

    private GradeResponse toGradeResponse(Grade grade) {
        GradeResponse response = new GradeResponse();
        response.setId(grade.getId());
        response.setName(grade.getName());
        response.setDescription(grade.getDescription());
        return response;
    }
}




