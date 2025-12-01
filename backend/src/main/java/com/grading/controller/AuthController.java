package com.grading.controller;

import com.grading.dto.request.LoginRequest;
import com.grading.dto.request.RegisterRequest;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.AuthResponse;
import com.grading.entity.Employee;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.UserRepository;
import com.grading.service.AuthService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Аутентификация и регистрация пользователей")
public class AuthController {
    private final AuthService authService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

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
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            try {
                Employee currentEmployee = getCurrentEmployee(authentication);
                if (!"hr".equalsIgnoreCase(currentEmployee.getRole())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Only HR can register new employees"));
                }
            } catch (Exception e) {
            }
        }
        else if (!"hr".equalsIgnoreCase(request.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Self-registration is only available for HR role"));
        }
        
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    private Employee getCurrentEmployee(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        return userRepository.findByUsername(username)
            .flatMap(user -> employeeRepository.findByUserId(user.getId()))
            .orElseThrow(() -> new RuntimeException("Employee not found for user: " + username));
    }
}