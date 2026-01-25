package com.grading.security;

import com.grading.entity.User;
import com.grading.repository.EmployeeRepository;
import com.grading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Get employee to determine role and authorities
        List<GrantedAuthority> authorities = employeeRepository.findByUserId(user.getId())
                .map(employee -> {
                    String role = employee.getRole();
                    if (role == null || role.trim().isEmpty()) {
                        return List.<GrantedAuthority>of();
                    }
                    return List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                })
                .orElse(List.<GrantedAuthority>of());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                authorities
        );
    }
}