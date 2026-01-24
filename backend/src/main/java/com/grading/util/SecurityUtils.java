package com.grading.util;

import com.grading.entity.Employee;
import com.grading.exception.ResourceNotFoundException;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public SecurityUtils(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    public Employee getCurrentEmployee(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Authentication required");
        }

        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            throw new ResourceNotFoundException("Invalid authentication principal");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        return userRepository.findByUsername(username)
            .flatMap(user -> employeeRepository.findByUserId(user.getId()))
            .orElseThrow(() -> new ResourceNotFoundException("Employee", username));
    }
}
