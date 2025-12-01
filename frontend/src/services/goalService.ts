import api from './api';
import { Goal, GoalTemplate, CreateGoalRequest, AssignGoalRequest, UpdateGoalStatusRequest, ApiResponse } from '@/types';

export const goalService = {
  // Goal Templates (HR only)
  createGoalTemplate: async (data: CreateGoalRequest): Promise<GoalTemplate> => {
    const response = await api.post<ApiResponse<GoalTemplate>>('/goal-templates', data);
    return response.data.data;
  },

  updateGoalTemplate: async (id: number, data: CreateGoalRequest): Promise<GoalTemplate> => {
    const response = await api.put<ApiResponse<GoalTemplate>>(`/goal-templates/${id}`, data);
    return response.data.data;
  },

  deleteGoalTemplate: async (id: number): Promise<void> => {
    await api.delete(`/goal-templates/${id}`);
  },

  getAllGoalTemplates: async (): Promise<GoalTemplate[]> => {
    const response = await api.get<ApiResponse<GoalTemplate[]>>('/goal-templates');
    return response.data.data;
  },

  getGoalTemplatesByReviewPeriod: async (reviewPeriod: string): Promise<GoalTemplate[]> => {
    const response = await api.get<ApiResponse<GoalTemplate[]>>(`/goal-templates/review-period/${reviewPeriod}`);
    return response.data.data;
  },

  // Goal Assignments
  assignGoal: async (data: AssignGoalRequest): Promise<Goal> => {
    const response = await api.post<ApiResponse<Goal>>('/goals/assign', data);
    return response.data.data;
  },

  updateGoalStatus: async (id: number, data: UpdateGoalStatusRequest): Promise<Goal> => {
    const response = await api.put<ApiResponse<Goal>>(`/goals/${id}/status`, data);
    return response.data.data;
  },

  deleteGoalAssignment: async (id: number): Promise<void> => {
    await api.delete(`/goals/${id}`);
  },

  getGoalsByEmployeeId: async (employeeId: number): Promise<Goal[]> => {
    const response = await api.get<ApiResponse<Goal[]>>(`/goals/employee/${employeeId}`);
    return response.data.data;
  },

  getMyGoals: async (): Promise<Goal[]> => {
    const response = await api.get<ApiResponse<Goal[]>>('/goals/my-goals');
    return response.data.data;
  },

  getAllGoalAssignments: async (): Promise<Goal[]> => {
    const response = await api.get<ApiResponse<Goal[]>>('/goals/all');
    return response.data.data;
  },
};