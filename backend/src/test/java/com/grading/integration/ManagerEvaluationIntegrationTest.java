package com.grading.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grading.dto.request.ManagerEvaluationRequest;
import com.grading.entity.Employee;
import com.grading.entity.ManagerEvaluation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ManagerEvaluationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateEvaluation_ByHR_ShouldSucceed() throws Exception {
        ManagerEvaluationRequest request = new ManagerEvaluationRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setReviewPeriod("2024-H1");
        request.setScore(new BigDecimal("4.5")); 
        request.setComment("Strong performance in all areas. Excellent problem-solving skills. " +
                "Needs improvement in time management."); 
        request.setNominatedForPromotion(false);

        String token = getAuthHeader("hruser");

        mockMvc.perform(post("/evaluations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data.score").value(4.5)) 
                .andExpect(jsonPath("$.data.comment").value("Strong performance in all areas. Excellent problem-solving skills. " +
                        "Needs improvement in time management.")) 
                .andExpect(jsonPath("$.data.nominatedForPromotion").value(false));
    }

    @Test
    void testCreateEvaluation_WithPromotionNomination_ShouldSucceed() throws Exception {
        ManagerEvaluationRequest request = new ManagerEvaluationRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setReviewPeriod("2024-H1");
        request.setScore(new BigDecimal("5.0"));
        request.setComment("Outstanding performance. Ready for promotion.");
        request.setNominatedForPromotion(true); 

        String token = getAuthHeader("hruser");

        mockMvc.perform(post("/evaluations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nominatedForPromotion").value(true)); 
    }

    @Test
    void testCreateEvaluation_ByEmployee_ShouldFail() throws Exception {
        ManagerEvaluationRequest request = new ManagerEvaluationRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setReviewPeriod("2024-H1");
        request.setScore(new BigDecimal("4.5"));
        request.setComment("Comment");
        request.setNominatedForPromotion(false);

        String token = getAuthHeader("testuser");

        mockMvc.perform(post("/evaluations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdateEvaluation_ByHR_ShouldSucceed() throws Exception {
        ManagerEvaluation evaluation = createManagerEvaluation(
                testHrEmployee, testEmployee, "2024-H1",
                new BigDecimal("4.0"), "Initial comment", false);

        ManagerEvaluationRequest request = new ManagerEvaluationRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setReviewPeriod("2024-H1");
        request.setScore(new BigDecimal("4.8")); 
        request.setComment("Updated: Excellent work, showing great improvement in all areas."); 
        request.setNominatedForPromotion(true); 

        String token = getAuthHeader("hruser");

        mockMvc.perform(put("/evaluations/{id}", evaluation.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(4.8))
                .andExpect(jsonPath("$.data.comment").value("Updated: Excellent work, showing great improvement in all areas."))
                .andExpect(jsonPath("$.data.nominatedForPromotion").value(true));
    }

    @Test
    void testGetEvaluationById_ByEmployee_ShouldReturnOwnEvaluation() throws Exception {
        ManagerEvaluation evaluation = createManagerEvaluation(
                testHrEmployee, testEmployee, "2024-H1",
                new BigDecimal("4.5"), "Good performance", false);

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/evaluations/{id}", evaluation.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(evaluation.getId()))
                .andExpect(jsonPath("$.data.score").value(4.5)) 
                .andExpect(jsonPath("$.data.comment").value("Good performance")); 
    }

    @Test
    void testGetEvaluationById_ByAnotherEmployee_ShouldFail() throws Exception {
        com.grading.entity.User anotherUser = createUser("anotheremployee", "password123");
        Employee anotherEmployee = createEmployee(anotherUser, "Another Employee", "another@example.com", "employee", "Engineering", "2024-H1");
        
        ManagerEvaluation evaluation = createManagerEvaluation(
                testHrEmployee, testEmployee, "2024-H1",
                new BigDecimal("4.5"), "Comment", false);

        String token = getAuthHeader("anotheremployee"); 

        mockMvc.perform(get("/evaluations/{id}", evaluation.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGetEvaluationsByEmployeeId_ShouldReturnList() throws Exception {
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H1",
                new BigDecimal("4.5"), "Comment 1", false);
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H2",
                new BigDecimal("4.8"), "Comment 2", true);

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/evaluations/employee/{employeeId}", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetAllEvaluations_ByHR_ShouldReturnAll() throws Exception {
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H1",
                new BigDecimal("4.5"), "Comment 1", false);
        createManagerEvaluation(testHrEmployee, testTeamLeadEmployee, "2024-H1",
                new BigDecimal("4.8"), "Comment 2", true);

        String token = getAuthHeader("hruser");

        mockMvc.perform(get("/evaluations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetAllEvaluations_ByEmployee_ShouldFail() throws Exception {
        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/evaluations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetEvaluationsByReviewPeriod_ByHR_ShouldReturnFilteredList() throws Exception {
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H1",
                new BigDecimal("4.5"), "Comment 1", false);
        createManagerEvaluation(testHrEmployee, testTeamLeadEmployee, "2024-H1",
                new BigDecimal("4.8"), "Comment 2", true);
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H2",
                new BigDecimal("4.6"), "Comment 3", false);

        String token = getAuthHeader("hruser");

        mockMvc.perform(get("/evaluations/review-period/{reviewPeriod}", "2024-H1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2)); 
    }

    @Test
    void testEvaluation_WithStrengthsAndWeaknesses_ShouldStoreComment() throws Exception {
        String comment = "Strengths: Excellent problem-solving, strong communication skills, " +
                "proactive approach. Weaknesses: Needs improvement in time management, " +
                "should focus more on documentation.";

        ManagerEvaluationRequest request = new ManagerEvaluationRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setReviewPeriod("2024-H1");
        request.setScore(new BigDecimal("4.2"));
        request.setComment(comment); 
        request.setNominatedForPromotion(false);

        String token = getAuthHeader("hruser");

        mockMvc.perform(post("/evaluations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.comment").value(comment)); 
    }

    @Test
    void testEvaluation_WithPromotionNomination_ShouldMarkCorrectly() throws Exception {
        ManagerEvaluationRequest request = new ManagerEvaluationRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setReviewPeriod("2024-H1");
        request.setScore(new BigDecimal("5.0"));
        request.setComment("Outstanding performance, ready for next level");
        request.setNominatedForPromotion(true); 

        String token = getAuthHeader("hruser");

        mockMvc.perform(post("/evaluations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nominatedForPromotion").value(true)); 

        mockMvc.perform(get("/evaluations/employee/{employeeId}", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nominatedForPromotion").value(true)); 
    }
}

