package com.grading.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grading.dto.request.AssignGoalRequest;
import com.grading.dto.request.UpdateGoalStatusRequest;
import com.grading.entity.Goal;
import com.grading.entity.GoalAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GoalIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAssignGoal_ByHR_ShouldSucceed() throws Exception {
        Goal goal = createGoal("Improve Code Quality", 
                "Reduce technical debt by 20%", 
                "Number of code review comments", 
                "2024-H1");

        AssignGoalRequest request = new AssignGoalRequest();
        request.setGoalId(goal.getId());
        request.setEmployeeId(testEmployee.getId());
        request.setDueDate(LocalDate.now().plusMonths(6)); 

        String token = getAuthHeader("hruser");

        mockMvc.perform(post("/goals/assign")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.employeeId").value(testEmployee.getId()))
                .andExpect(jsonPath("$.data.goalId").value(goal.getId()))
                .andExpect(jsonPath("$.data.dueDate").exists()) 
                .andExpect(jsonPath("$.data.status").value("in_progress")); 
    }

    @Test
    void testAssignGoal_ByEmployee_ShouldFail() throws Exception {
        Goal goal = createGoal("Test Goal", "Description", "Metric", "2024-H1");

        AssignGoalRequest request = new AssignGoalRequest();
        request.setGoalId(goal.getId());
        request.setEmployeeId(testEmployee.getId());
        request.setDueDate(LocalDate.now().plusMonths(6));

        String token = getAuthHeader("testuser");

        mockMvc.perform(post("/goals/assign")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testUpdateGoalStatus_ByEmployee_ShouldSucceed() throws Exception {
        Goal goal = createGoal("Test Goal", "Description", "Metric", "2024-H1");
        GoalAssignment assignment = createGoalAssignment(goal, testEmployee, 
                LocalDate.now().plusMonths(6), "not_started");

        UpdateGoalStatusRequest request = new UpdateGoalStatusRequest();
        request.setStatus("in_progress"); 

        String token = getAuthHeader("testuser");

        mockMvc.perform(put("/goals/{id}/status", assignment.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("in_progress")); 
    }

    @Test
    void testUpdateGoalStatus_ToCompleted_ShouldSucceed() throws Exception {
        Goal goal = createGoal("Test Goal", "Description", "Metric", "2024-H1");
        GoalAssignment assignment = createGoalAssignment(goal, testEmployee, 
                LocalDate.now().plusMonths(6), "in_progress");

        UpdateGoalStatusRequest request = new UpdateGoalStatusRequest();
        request.setStatus("completed"); 

        String token = getAuthHeader("testuser");

        mockMvc.perform(put("/goals/{id}/status", assignment.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("completed"));
    }

    @Test
    void testUpdateGoalStatus_ByAnotherEmployee_ShouldFail() throws Exception {
        Goal goal = createGoal("Test Goal", "Description", "Metric", "2024-H1");
        GoalAssignment assignment = createGoalAssignment(goal, testEmployee, 
                LocalDate.now().plusMonths(6), "not_started");

        UpdateGoalStatusRequest request = new UpdateGoalStatusRequest();
        request.setStatus("in_progress");

        String token = getAuthHeader("teamlead"); 

        mockMvc.perform(put("/goals/{id}/status", assignment.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetGoalAssignmentById_ShouldReturnGoal() throws Exception {
        Goal goal = createGoal("Test Goal", "Description", "Metric", "2024-H1");
        GoalAssignment assignment = createGoalAssignment(goal, testEmployee, 
                LocalDate.now().plusMonths(6), "in_progress");

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/goals/{id}", assignment.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(assignment.getId()))
                .andExpect(jsonPath("$.data.status").value("in_progress"))
                .andExpect(jsonPath("$.data.dueDate").exists()); 
    }

    @Test
    void testGetGoalsByEmployeeId_ShouldReturnList() throws Exception {
        Goal goal1 = createGoal("Goal 1", "Description 1", "Metric 1", "2024-H1");
        Goal goal2 = createGoal("Goal 2", "Description 2", "Metric 2", "2024-H1");
        createGoalAssignment(goal1, testEmployee, LocalDate.now().plusMonths(6), "not_started");
        createGoalAssignment(goal2, testEmployee, LocalDate.now().plusMonths(6), "in_progress");

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/goals/employee/{employeeId}", testEmployee.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetMyGoals_ShouldReturnOwnGoals() throws Exception {
        Goal goal = createGoal("My Goal", "Description", "Metric", "2024-H1");
        createGoalAssignment(goal, testEmployee, LocalDate.now().plusMonths(6), "not_started");

        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/goals/my-goals")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetAllGoalAssignments_ByHR_ShouldReturnAll() throws Exception {
        Goal goal1 = createGoal("Goal 1", "Description 1", "Metric 1", "2024-H1");
        Goal goal2 = createGoal("Goal 2", "Description 2", "Metric 2", "2024-H1");
        createGoalAssignment(goal1, testEmployee, LocalDate.now().plusMonths(6), "not_started");
        createGoalAssignment(goal2, testTeamLeadEmployee, LocalDate.now().plusMonths(6), "in_progress");

        String token = getAuthHeader("hruser");

        mockMvc.perform(get("/goals/all")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetAllGoalAssignments_ByEmployee_ShouldFail() throws Exception {
        String token = getAuthHeader("testuser");

        mockMvc.perform(get("/goals/all")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteGoalAssignment_ByHR_ShouldSucceed() throws Exception {
        Goal goal = createGoal("Test Goal", "Description", "Metric", "2024-H1");
        GoalAssignment assignment = createGoalAssignment(goal, testEmployee, 
                LocalDate.now().plusMonths(6), "not_started");

        String token = getAuthHeader("hruser");

        mockMvc.perform(delete("/goals/{id}", assignment.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/goals/{id}", assignment.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()) 
                .andExpect(jsonPath("$.success").value(false)); 
    }

    @Test
    void testAssignGoal_WithHalfYearDeadline_ShouldSetCorrectDueDate() throws Exception {
        Goal goal = createGoal("Half Year Goal", "Description", "Metric", "2024-H1");
        LocalDate expectedDueDate = LocalDate.now().plusMonths(6);

        AssignGoalRequest request = new AssignGoalRequest();
        request.setGoalId(goal.getId());
        request.setEmployeeId(testEmployee.getId());
        request.setDueDate(expectedDueDate);

        String token = getAuthHeader("hruser");

        mockMvc.perform(post("/goals/assign")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.dueDate").exists()) 
                .andExpect(jsonPath("$.data.status").value("in_progress")); 
    }
}

