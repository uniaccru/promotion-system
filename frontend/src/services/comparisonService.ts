import api from './api';
import { Comparison, CreateComparisonRequest, ApiResponse } from '@/types';

export const comparisonService = {
  createComparison: async (request: CreateComparisonRequest): Promise<Comparison> => {
    const response = await api.post<ApiResponse<Comparison>>('/comparisons', request);
    return response.data.data;
  },

  getComparisonsByCalibrationId: async (calibrationId: number): Promise<Comparison[]> => {
    const response = await api.get<ApiResponse<Comparison[]>>(`/comparisons/calibration/${calibrationId}`);
    return response.data.data;
  },

  getPendingComparisons: async (calibrationId: number): Promise<Comparison[]> => {
    const response = await api.get<ApiResponse<Comparison[]>>(`/comparisons/calibration/${calibrationId}/pending`);
    return response.data.data;
  },
};

