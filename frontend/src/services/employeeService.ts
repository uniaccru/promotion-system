import api from './api';
import { Employee, EmployeeProfile, ApiResponse } from '@/types';

export const employeeService = {
  getProfile: async (id: number): Promise<EmployeeProfile> => {
    const response = await api.get<ApiResponse<EmployeeProfile>>(`/employees/${id}/profile`);
    return response.data.data;
  },

  getAllEmployees: async (): Promise<Employee[]> => {
    const response = await api.get<ApiResponse<Employee[]>>('/employees');
    return response.data.data;
  },

  getEmployeeById: async (id: number): Promise<Employee> => {
    const response = await api.get<ApiResponse<Employee>>(`/employees/${id}`);
    return response.data.data;
  },
};