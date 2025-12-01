import api from './api';
import { Calibration, CreateCalibrationPackageRequest, CandidateRanking, ApiResponse } from '@/types';

export const calibrationService = {
  createCalibration: async (gradeId: number): Promise<Calibration> => {
    const response = await api.post<ApiResponse<Calibration>>(`/calibrations?gradeId=${gradeId}`);
    return response.data.data;
  },

  createCalibrationPackage: async (request: CreateCalibrationPackageRequest): Promise<Calibration> => {
    const response = await api.post<ApiResponse<Calibration>>('/calibrations/package', request);
    return response.data.data;
  },

  updateStatus: async (id: number, status: string): Promise<Calibration> => {
    const response = await api.put<ApiResponse<Calibration>>(`/calibrations/${id}/status?status=${status}`);
    return response.data.data;
  },

  getCalibrationsByStatus: async (status: string): Promise<Calibration[]> => {
    const response = await api.get<ApiResponse<Calibration[]>>(`/calibrations/status/${status}`);
    return response.data.data;
  },

  getCalibrationDetails: async (id: number): Promise<Calibration> => {
    const response = await api.get<ApiResponse<Calibration>>(`/calibrations/${id}/details`);
    return response.data.data;
  },

  getCalibrationsByEvaluatorId: async (evaluatorId: number): Promise<Calibration[]> => {
    const response = await api.get<ApiResponse<Calibration[]>>(`/calibrations/evaluator/${evaluatorId}`);
    return response.data.data;
  },

  getCandidateRanking: async (id: number): Promise<CandidateRanking> => {
    const response = await api.get<ApiResponse<CandidateRanking>>(`/calibrations/${id}/ranking`);
    return response.data.data;
  },
};