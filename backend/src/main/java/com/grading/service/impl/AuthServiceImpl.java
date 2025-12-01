package com.grading.service.impl;

import com.grading.dto.request.LoginRequest;
import com.grading.dto.request.RegisterRequest;
import com.grading.dto.response.AuthResponse;
import com.grading.entity.Employee;
import com.grading.entity.Grade;
import com.grading.entity.GradeHistory;
import com.grading.entity.User;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.GradeHistoryRepository;
import com.grading.repository.GradeRepository;
import com.grading.repository.UserRepository;
import com.grading.security.JwtTokenProvider;
import com.grading.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final GradeRepository gradeRepository;
    private final GradeHistoryRepository gradeHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        Employee employee = employeeRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        return new AuthResponse(token, user.getId(), employee.getId(), user.getUsername(), employee.getRole());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Grade initialGrade = gradeRepository.findById(request.getInitialGradeId())
            .orElseThrow(() -> new RuntimeException("Initial grade not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setRole(request.getRole());
        employee.setHireDate(request.getHireDate());
        employee.setDepartment(request.getDepartment());
        employee.setReviewPeriod(request.getReviewPeriod());
        employee = employeeRepository.save(employee);

        GradeHistory gradeHistory = new GradeHistory();
        gradeHistory.setEmployee(employee);
        gradeHistory.setOldGrade(initialGrade);
        gradeHistory.setNewGrade(initialGrade);
        gradeHistory.setChangedBy(employee);
        gradeHistory.setReason("Initial grade assignment");
        gradeHistoryRepository.save(gradeHistory);

        return new AuthResponse(null, user.getId(), employee.getId(), user.getUsername(), employee.getRole());
    }
}