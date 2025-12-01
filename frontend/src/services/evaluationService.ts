import api from './api';
import { ManagerEvaluation, EvaluationRequest, ApiResponse } from '@/types';

export const evaluationService = {
  createEvaluation: async (data: EvaluationRequest): Promise<ManagerEvaluation> => {
    const response = await api.post<ApiResponse<ManagerEvaluation>>('/evaluations', data);
    return response.data.data;
  },

  getEvaluationsByEmployeeId: async (employeeId: number): Promise<ManagerEvaluation[]> => {
    const response = await api.get<ApiResponse<ManagerEvaluation[]>>(`/evaluations/employee/${employeeId}`);
    return response.data.data;
  },

  getAllEvaluations: async (): Promise<ManagerEvaluation[]> => {
    const response = await api.get<ApiResponse<ManagerEvaluation[]>>('/evaluations');
    return response.data.data;
  },

  getEvaluationById: async (id: number): Promise<ManagerEvaluation> => {
    const response = await api.get<ApiResponse<ManagerEvaluation>>(`/evaluations/${id}`);
    return response.data.data;
  },
};