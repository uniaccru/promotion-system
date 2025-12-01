package com.grading.integration;

import com.grading.entity.*;
import com.grading.repository.*;
import com.grading.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected EmployeeRepository employeeRepository;

    @Autowired
    protected GradeRepository gradeRepository;

    @Autowired
    protected GradeHistoryRepository gradeHistoryRepository;

    @Autowired
    protected GoalRepository goalRepository;

    @Autowired
    protected GoalAssignmentRepository goalAssignmentRepository;

    @Autowired
    protected ManagerEvaluationRepository managerEvaluationRepository;

    @Autowired
    protected PromotionRequestRepository promotionRequestRepository;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Employee testEmployee;
    protected Employee testHrEmployee;
    protected Employee testTeamLeadEmployee;
    protected com.grading.entity.User testUser;
    protected com.grading.entity.User testHrUser;
    protected com.grading.entity.User testTeamLeadUser;
    protected Grade testGrade1;
    protected Grade testGrade2;
    protected Grade testGrade3;

    @BeforeEach
    void setUp() {
        testGrade1 = createGrade("Junior", "Junior developer grade");
        testGrade2 = createGrade("Middle", "Middle developer grade");
        testGrade3 = createGrade("Senior", "Senior developer grade");

        testUser = createUser("testuser", "password123");
        testEmployee = createEmployee(testUser, "Test Employee", "test@example.com", "employee", "Engineering", "2024-H1");

        testHrUser = createUser("hruser", "password123");
        testHrEmployee = createEmployee(testHrUser, "HR Employee", "hr@example.com", "hr", "HR", "2024-H1");

        testTeamLeadUser = createUser("teamlead", "password123");
        testTeamLeadEmployee = createEmployee(testTeamLeadUser, "Team Lead", "teamlead@example.com", "team_lead", "Engineering", "2024-H1");
    }

    protected com.grading.entity.User createUser(String username, String password) {
        com.grading.entity.User user = new com.grading.entity.User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    protected Employee createEmployee(com.grading.entity.User user, String fullName, String email, String role, String department, String reviewPeriod) {
        Employee employee = new Employee();
        employee.setUser(user);
        employee.setFullName(fullName);
        employee.setEmail(email);
        employee.setRole(role);
        employee.setDepartment(department);
        employee.setReviewPeriod(reviewPeriod);
        employee.setHireDate(LocalDate.now().minusYears(1));
        return employeeRepository.save(employee);
    }

    protected Grade createGrade(String name, String description) {
        Grade grade = new Grade();
        grade.setName(name);
        grade.setDescription(description);
        return gradeRepository.save(grade);
    }

    protected GradeHistory createGradeHistory(Employee employee, Grade oldGrade, Grade newGrade, Employee changedBy, String reason) {
        GradeHistory history = new GradeHistory();
        history.setEmployee(employee);
        history.setOldGrade(oldGrade);
        history.setNewGrade(newGrade);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        return gradeHistoryRepository.save(history);
    }

    protected Goal createGoal(String title, String description, String metric, String reviewPeriod) {
        Goal goal = new Goal();
        goal.setTitle(title);
        goal.setDescription(description);
        goal.setMetric(metric);
        goal.setReviewPeriod(reviewPeriod);
        return goalRepository.save(goal);
    }

    protected GoalAssignment createGoalAssignment(Goal goal, Employee employee, LocalDate dueDate, String status) {
        GoalAssignment assignment = new GoalAssignment();
        assignment.setGoal(goal);
        assignment.setEmployee(employee);
        assignment.setDueDate(dueDate);
        assignment.setStatus(status);
        return goalAssignmentRepository.save(assignment);
    }

    protected ManagerEvaluation createManagerEvaluation(Employee evaluator, Employee employee, String reviewPeriod, 
                                                         BigDecimal score, String comment, Boolean nominatedForPromotion) {
        ManagerEvaluation evaluation = new ManagerEvaluation();
        evaluation.setEvaluator(evaluator);
        evaluation.setEmployee(employee);
        evaluation.setReviewPeriod(reviewPeriod);
        evaluation.setScore(score);
        evaluation.setComment(comment);
        evaluation.setNominatedForPromotion(nominatedForPromotion);
        return managerEvaluationRepository.save(evaluation);
    }

    protected PromotionRequest createPromotionRequest(Employee employee, Grade requestedGrade, Employee submittedBy, 
                                                      Employee statusChangedBy, String justification, String evidence, 
                                                      String reviewPeriod, String status) {
        PromotionRequest request = new PromotionRequest();
        request.setEmployee(employee);
        request.setRequestedGrade(requestedGrade);
        request.setSubmittedBy(submittedBy);
        request.setStatusChangedBy(statusChangedBy);
        request.setJustification(justification);
        request.setEvidence(evidence);
        request.setReviewPeriod(reviewPeriod);
        request.setStatus(status);
        return promotionRequestRepository.save(request);
    }

    protected String generateToken(String username) {
        com.grading.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        UserDetails userDetails = User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(Collections.emptyList())
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return jwtTokenProvider.generateToken(authentication);
    }

    protected String getAuthHeader(String username) {
        return "Bearer " + generateToken(username);
    }
}

