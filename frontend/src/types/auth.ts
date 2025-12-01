export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  email: string;
  role: string;
  hireDate: string;
  department: string;
  reviewPeriod: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  employeeId: number;
  username: string;
  role: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}