export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface Employee {
  id: number;
  fullName: string;
  email: string;
  role: string;
  hireDate: string;
  department: string;
  reviewPeriod: string;
}

export interface EmployeeProfile extends Employee {
  currentGrade: string;
  lastReviewDate: string | null;
  lastScore: number | null;
  gradeChangesCount: number;
}

export interface Goal {
  id: number;
  goalId: number;
  goalTitle: string;
  goalDescription: string;
  goalMetric: string;
  employeeId: number;
  employeeName?: string;
  dueDate: string;
  status: string;
  reviewPeriod: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface GoalTemplate {
  id: number;
  title: string;
  description: string;
  metric: string;
  reviewPeriod: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateGoalRequest {
  title: string;
  description: string;
  metric: string;
  reviewPeriod: string;
}

export interface AssignGoalRequest {
  goalId: number;
  employeeId: number;
  dueDate: string;
}

export interface UpdateGoalStatusRequest {
  status: string;
}

export interface ManagerEvaluation {
  id: number;
  evaluatorId: number;
  evaluatorName: string;
  employeeId: number;
  employeeName: string;
  reviewPeriod: string;
  score: number;
  comment: string;
  nominatedForPromotion: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EvaluationRequest {
  employeeId: number;
  reviewPeriod: string;
  score: number;
  comment: string;
  nominatedForPromotion: boolean;
}

export interface PromotionRequest {
  id: number;
  employeeId: number;
  employeeName: string;
  requestedGradeId: number;
  requestedGradeName: string;
  submittedById: number;
  submittedByName: string;
  justification: string;
  evidence: string;
  reviewPeriod: string;
  status: string;
  hrComment?: string;
  createdAt: string;
}

export interface Grade {
  id: number;
  name: string;
  description: string;
}

export interface PromotionRequestData {
  employeeId: number;
  requestedGradeId: number;
  justification: string;
  evidence: string;
  reviewPeriod: string;
}

export interface Calibration {
  id: number;
  gradeId: number;
  gradeName?: string;
  createdById?: number;
  createdByName?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  evaluatorIds?: number[];
  evaluatorNames?: string[];
  promotionRequestIds?: number[];
  candidateCount?: number;
}

export interface CreateCalibrationPackageRequest {
  gradeId: number;
  promotionRequestIds: number[];
  evaluatorIds: number[];
}

export interface Comparison {
  id?: number;
  calibrationId: number;
  candidateAId: number;
  candidateAName: string;
  candidateAJustification?: string;
  candidateBId: number;
  candidateBName: string;
  candidateBJustification?: string;
  winnerId?: number;
  winnerName?: string;
  decidedById?: number;
  decidedByName?: string;
  decidedAt?: string;
}

export interface CreateComparisonRequest {
  calibrationId: number;
  candidateAId: number;
  candidateBId: number;
  winnerId: number;
}

export interface CandidateRanking {
  calibrationId: number;
  rankings: CandidateScore[];
}

export interface CandidateScore {
  employeeId: number;
  employeeName: string;
  promotionRequestId: number;
  requestedGradeName: string;
  currentStatus: string;
  wins: number;
  totalComparisons: number;
  winRate: number;
}