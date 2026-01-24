package com.grading.controller;

import com.grading.dto.request.LoginRequest;
import com.grading.dto.request.RegisterRequest;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.AuthResponse;
import com.grading.entity.Employee;
import com.grading.exception.ForbiddenException;
import com.grading.service.AuthService;
import com.grading.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Аутентификация и регистрация пользователей")
public class AuthController {
    private final AuthService authService;
    private final SecurityUtils securityUtils;

    @PostMapping("/login")
    @Operation(
        summary = "Вход в систему",
        description = "Аутентификация пользователя по username и password. Возвращает JWT токен."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Успешная аутентификация",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Неверные учетные данные"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(
        summary = "Регистрация нового сотрудника",
        description = "Создание нового пользователя и сотрудника в системе. Если пользователь аутентифицирован - должен быть HR. Если нет - может регистрироваться как HR."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Успешная регистрация",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Пользователь с таким username уже существует"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Доступ запрещён - только HR может регистрировать пользователей"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            Authentication authentication) {
        boolean isAuthenticatedHR = false;
        
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                Employee currentEmployee = securityUtils.getCurrentEmployee(authentication);
                if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
                    throw new ForbiddenException("Only HR can register new employees");
                }
                isAuthenticatedHR = true;
            } catch (com.grading.exception.ResourceNotFoundException e) {
                // If employee not found, treat as not authenticated
            }
        }
        
        // If not authenticated as HR, only allow self-registration as HR
        if (!isAuthenticatedHR && !"hr".equalsIgnoreCase(request.getRole())) {
            throw new ForbiddenException("Self-registration is only available for HR role");
        }
        
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }
}