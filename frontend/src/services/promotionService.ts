import api from './api';
import { PromotionRequest, PromotionRequestData, ApiResponse, Goal } from '@/types';

export const promotionService = {
  createPromotionRequest: async (data: PromotionRequestData): Promise<PromotionRequest> => {
    const response = await api.post<ApiResponse<PromotionRequest>>('/promotion-requests', data);
    return response.data.data;
  },

  updatePromotionRequest: async (id: number, data: PromotionRequestData): Promise<PromotionRequest> => {
    const response = await api.put<ApiResponse<PromotionRequest>>(`/promotion-requests/${id}`, data);
    return response.data.data;
  },

  deletePromotionRequest: async (id: number): Promise<void> => {
    await api.delete(`/promotion-requests/${id}`);
  },

  updateStatus: async (id: number, status: string, comment?: string): Promise<PromotionRequest> => {
    let url = `/promotion-requests/${id}/status?status=${status}`;
    if (comment) {
      url += `&comment=${encodeURIComponent(comment)}`;
    }
    const response = await api.put<ApiResponse<PromotionRequest>>(url);
    return response.data.data;
  },

  getPromotionRequestsByEmployeeId: async (employeeId: number): Promise<PromotionRequest[]> => {
    const response = await api.get<ApiResponse<PromotionRequest[]>>(`/promotion-requests/employee/${employeeId}`);
    return response.data.data;
  },

  getPromotionRequestsByStatus: async (status: string): Promise<PromotionRequest[]> => {
    const response = await api.get<ApiResponse<PromotionRequest[]>>(`/promotion-requests/status/${status}`);
    return response.data.data;
  },

  getAllPromotionRequests: async (): Promise<PromotionRequest[]> => {
    const response = await api.get<ApiResponse<PromotionRequest[]>>('/promotion-requests');
    return response.data.data;
  },

  getPromotionRequestById: async (id: number): Promise<PromotionRequest> => {
    const response = await api.get<ApiResponse<PromotionRequest>>(`/promotion-requests/${id}`);
    return response.data.data;
  },

  approveOrRejectPromotion: async (id: number, decision: 'approved' | 'rejected', comment?: string): Promise<PromotionRequest> => {
    const response = await api.post<ApiResponse<PromotionRequest>>(`/promotion-requests/${id}/decision`, {
      decision,
      comment,
    });
    return response.data.data;
  },

  attachGoalsToPromotionRequest: async (id: number, goalAssignmentIds: number[]): Promise<void> => {
    await api.post(`/promotion-requests/${id}/goals`, goalAssignmentIds);
  },

  detachGoalFromPromotionRequest: async (id: number, goalAssignmentId: number): Promise<void> => {
    await api.delete(`/promotion-requests/${id}/goals/${goalAssignmentId}`);
  },

  getPromotionRequestGoals: async (id: number): Promise<Goal[]> => {
    const response = await api.get<ApiResponse<Goal[]>>(`/promotion-requests/${id}/goals`);
    return response.data.data;
  },

  uploadFile: async (id: number, file: File): Promise<any> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post(`/promotion-requests/${id}/files`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data.data;
  },

  getPromotionRequestFiles: async (id: number): Promise<any[]> => {
    const response = await api.get<ApiResponse<any[]>>(`/promotion-requests/${id}/files`);
    return response.data.data;
  },

  downloadFile: async (fileId: number): Promise<void> => {
    const response = await api.get(`/promotion-requests/files/${fileId}/download`, {
      responseType: 'blob',
    });
    
    // Create blob link to download
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    
    // Get filename from content-disposition header
    const contentDisposition = response.headers['content-disposition'];
    let filename = 'download';
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="(.+)"/);
      if (filenameMatch) {
        filename = filenameMatch[1];
      }
    }
    
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  deleteFile: async (fileId: number): Promise<void> => {
    await api.delete(`/promotion-requests/files/${fileId}`);
  },
};