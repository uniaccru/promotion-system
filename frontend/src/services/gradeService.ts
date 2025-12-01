import api from './api';
import { Grade, ApiResponse } from '@/types';

export const gradeService = {
  getAllGrades: async (): Promise<Grade[]> => {
    const response = await api.get<ApiResponse<Grade[]>>('/grades');
    return response.data.data;
  },

  getGradeById: async (id: number): Promise<Grade> => {
    const response = await api.get<ApiResponse<Grade>>(`/grades/${id}`);
    return response.data.data;
  },
};




