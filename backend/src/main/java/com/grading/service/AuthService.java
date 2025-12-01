package com.grading.service;

import com.grading.dto.request.LoginRequest;
import com.grading.dto.request.RegisterRequest;
import com.grading.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}