package com.grading.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grading.dto.request.PromotionRequestRequest;
import com.grading.entity.PromotionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PromotionRequestIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePromotionRequest_ByEmployee_ShouldSucceed() throws Exception {
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H1", 
                new java.math.BigDecimal("4.5"), "Good performance", false);

        PromotionRequestRequest request = new PromotionRequestRequest();
        request.setEmployeeId(testEmployee.getId());
        request.setRequestedGradeId(testGrade2.getId());
        request.setJustification("I have completed all goals and demonstrated strong performance");
        request.setEvidence("https:
        request.setReviewPeriod("2024-H1");

        String token = getAuthHeader("testuser");

        mockMvc.perform(post("/promotion-requests")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data.requestedGradeId").value(testGrade2.getId()))
                .andExpect(jsonPath("$.data.justification").value("I have completed all goals and demonstrated strong performance"))
                .andExpect(jsonPath("$.data.evidence").value("https:
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    void testCreatePromotionRequest_ByTeamLead_ShouldSucceed() throws Exception {
        createManagerEvaluation(testHrEmployee, testTeamLeadEmployee, "2024-H1", 
                new java.math.BigDecimal("4.8"), "Excellent performance", false);

        PromotionRequestRequest request = new PromotionRequestRequest();
        request.setEmployeeId(testTeamLeadEmployee.getId());
        request.setRequestedGradeId(testGrade3.getId());
        request.setJustification("Team lead justification");
        request.setEvidence("https:
        request.setReviewPeriod("2024-H1");

        String token = getAuthHeader("teamlead");

        mockMvc.perform(post("/promotion-requests")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    void testCreatePromotionRequest_ForAnotherEmployee_ShouldFail() throws Exception {
        PromotionRequestRequest request = new PromotionRequestRequest();
        request.setEmployeeId(testHrEmployee.getId()); 
        request.setRequestedGradeId(testGrade2.getId());
        request.setJustification("Justification");
        request.setEvidence("Evidence");
        request.setReviewPeriod("2024-H1");

        String token = getAuthHeader("testuser");

        mockMvc.perform(post("/promotion-requests")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testCreatePromotionRequest_DuplicatePending_ShouldBeBlocked() throws Exception {
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H1", 
                new java.math.BigDecimal("4.5"), "Good performance", false);

        PromotionRequestRequest request1 = new PromotionRequestRequest();
        request1.setEmployeeId(testEmployee.getId());
        request1.setRequestedGradeId(testGrade2.getId());
        request1.setJustification("First request");
        request1.setEvidence("Evidence1");
        request1.setReviewPeriod("2024-H1");

        String token = getAuthHeader("testuser");

        mockMvc.perform(post("/promotion-requests")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        PromotionRequestRequest request2 = new PromotionRequestRequest();
        request2.setEmployeeId(testEmployee.getId());
        request2.setRequestedGradeId(testGrade2.getId());
        request2.setJustification("Duplicate request");
        request2.setEvidence("Evidence2");
        request2.setReviewPeriod("2024-H1");

        mockMvc.perform(post("/promotion-requests")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isInternalServerError()) 
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Active promotion request already exists")));
    }

    @Test
    void testUpdatePromotionRequest_ByAuthor_ShouldSucceed() throws Exception {
        createManagerEvaluation(testHrEmployee, testEmployee, "2024-H1", 
                new java.math.BigDecimal("4.5"), "Good performance", false);

        PromotionRequestRequest createRequest = new PromotionRequestRequest();
        createRequest.setEmployeeId(testEmployee.getId());
        createRequest.setRequestedGradeId(testGrade2.getId());
        createRequest.setJustification("Initial justification");
        createRequest.setEvidence("Initial evidence");
        createRequest.setReviewPeriod("2024-H1");

        String token = getAuthHeader("testuser");

        String response = mockMvc.perform(post("/promotion-requests")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long requestId = extractIdFromResponse(response);

        PromotionRequestRequest updateRequest = new PromotionRequestRequest();
        updateRequest.setEmployeeId(testEmployee.getId());
        updateRequest.setRequestedGradeId(testGrade2.getId());
        updateRequest.setJustification("Updated justification");
        updateRequest.setEvidence("Updated evidence");
        updateRequest.setReviewPeriod("2024-H1");

        mockMvc.perform(put("/promotion-requests/{id}", requestId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.justification").value("Updated justification"));
    }

    @Test
    void testUpdatePromotionRequestStatus_ByHR_ShouldSucceed() throws Exception {
        PromotionRequest request = createPromotionRequest(
                testEmployee, testGrade2, testEmployee, testHrEmployee,
                "Justification", "Evidence", "2024-H1", "pending");

        String token = getAuthHeader("hruser");

        mockMvc.perform(put("/promotion-requests/{id}/status", request.getId())
                        .header("Authorization", token)
                        .param("status", "under_review")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("under_review"));
    }

    @Test
    void testGetPromotionRequest_ByEmployee_ShouldReturnOwnRequest() throws Exception {
        PromotionRequest request = createPromotionRequest(
                testEmployee, testGrade2, testEmployee, testHrEmployee,
                "Justification", "Evidence", "2024-H1", "pending");

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/promotion-requests/{id}", request.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(request.getId()))
                .andExpect(jsonPath("$.data.justification").value("Justification"))
                .andExpect(jsonPath("$.data.evidence").value("Evidence")); 
    }

    @Test
    void testGetPromotionRequestsByEmployee_ShouldReturnList() throws Exception {
        createPromotionRequest(testEmployee, testGrade2, testEmployee, testHrEmployee,
                "Request 1", "Evidence 1", "2024-H1", "pending");
        createPromotionRequest(testEmployee, testGrade3, testEmployee, testHrEmployee,
                "Request 2", "Evidence 2", "2024-H2", "pending");

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/promotion-requests/employee/{employeeId}", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetAllPromotionRequests_ByHR_ShouldReturnAll() throws Exception {
        createPromotionRequest(testEmployee, testGrade2, testEmployee, testHrEmployee,
                "Request 1", "Evidence 1", "2024-H1", "pending");
        createPromotionRequest(testTeamLeadEmployee, testGrade3, testTeamLeadEmployee, testHrEmployee,
                "Request 2", "Evidence 2", "2024-H1", "pending");

        String token = getAuthHeader("hruser");

        mockMvc.perform(get("/promotion-requests")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetAllPromotionRequests_ByEmployee_ShouldFail() throws Exception {
        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/promotion-requests")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    private Long extractIdFromResponse(String response) {
        try {
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("data").get("id").asLong();
        } catch (Exception e) {
            return 1L;
        }
    }
}

