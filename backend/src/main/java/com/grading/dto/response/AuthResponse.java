package com.grading.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private Long employeeId;
    private String username;
    private String role;

    public AuthResponse(String token, Long userId, Long employeeId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.employeeId = employeeId;
        this.username = username;
        this.role = role;
    }
}