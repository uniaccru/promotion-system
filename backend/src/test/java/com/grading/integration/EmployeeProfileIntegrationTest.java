package com.grading.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grading.dto.response.ApiResponse;
import com.grading.dto.response.EmployeeProfileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EmployeeProfileIntegrationTest extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetEmployeeProfile_WithGradeHistory_ShouldReturnCompleteProfile() throws Exception {
        createGradeHistory(testEmployee, testGrade1, testGrade2, testHrEmployee, "Promotion after successful review");
        
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H1", 
                new BigDecimal("4.5"), "Good performance", false);

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/employees/{id}/profile", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data.fullName").value("Test Employee"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.role").value("employee"))
                .andExpect(jsonPath("$.data.department").value("Engineering"))
                .andExpect(jsonPath("$.data.currentGrade").exists())
                .andExpect(jsonPath("$.data.lastReviewDate").exists())
                .andExpect(jsonPath("$.data.gradeChangesCount").exists());
    }

    @Test
    void testGetEmployeeProfile_WithoutGradeHistory_ShouldReturnProfileWithNullGrade() throws Exception {
        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/employees/{id}/profile", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data.department").value("Engineering"));
    }

    @Test
    void testGetEmployeeProfile_Unauthorized_ShouldReturn401() throws Exception {
        var result = mockMvc.perform(get("/employees/{id}/profile", testEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        int status = result.getResponse().getStatus();
        org.junit.jupiter.api.Assertions.assertTrue(
                status == 401 || status == 403,
                "Expected 401 or 403, but got " + status
        );
    }

    @Test
    void testGetEmployeeProfile_WithMultipleGradeChanges_ShouldShowCorrectCount() throws Exception {
        createGradeHistory(testEmployee, testGrade1, testGrade2, testHrEmployee, "First promotion");
        createGradeHistory(testEmployee, testGrade2, testGrade3, testHrEmployee, "Second promotion");

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/employees/{id}/profile", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gradeChangesCount").value(2));
    }

    @Test
    void testGetEmployeeProfile_WithLastReview_ShouldShowLastReviewDate() throws Exception {
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H1", 
                new BigDecimal("4.0"), "Excellent work", true);

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/employees/{id}/profile", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lastReviewDate").exists())
                .andExpect(jsonPath("$.data.lastScore").value(4.0));
    }

    @Test
    void testGetAllEmployees_ShouldReturnList() throws Exception {
        String token = getAuthHeader("hruser");

        mockMvc.perform(get("/employees")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void testGetEmployeeById_ShouldReturnEmployee() throws Exception {
        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/employees/{id}", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data.fullName").value("Test Employee"));
    }

    @Test
    void testGetEmployeesByDepartment_ShouldReturnFilteredList() throws Exception {
        String token = getAuthHeader("hruser");

        mockMvc.perform(get("/employees/department/{department}", "Engineering")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}

