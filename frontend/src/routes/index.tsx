import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import DashboardLayout from '@/layouts/DashboardLayout';
import EmployeeProfilePage from '@/pages/EmloyeeProfilePage';
import GoalsPage from '@/pages/GoalsPage';
import ReviewsPage from '@/pages/ReviewsPage';
import PromotionRequestsPage from '@/pages/PromotionRequestPage';
import CalibrationPage from '@/pages/CalibrationPage';
import EmployeeManagementPage from '@/pages/EmployeeManagementPage';

// Protected route wrapper component
const ProtectedRoute = ({ children, allowedRoles }: { children: JSX.Element; allowedRoles: string[] }) => {
  const { user } = useAuth();
  const userRole = user?.role?.toLowerCase() || '';
  
  const hasAccess = allowedRoles.some(role => {
    if (role === 'team_lead') {
      return userRole === 'team_lead';
    }
    return userRole === role;
  });

  if (!hasAccess) {
    return <Navigate to="/dashboard/profile" />;
  }

  return children;
};

const AppRoutes = () => {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/dashboard" />} />
      <Route path="/register" element={<RegisterPage />} />
      
      <Route path="/dashboard" element={isAuthenticated ? <DashboardLayout /> : <Navigate to="/login" />}>
        <Route index element={<Navigate to="/dashboard/profile" />} />
        <Route path="profile" element={
          <ProtectedRoute allowedRoles={['employee', 'team_lead', 'hr']}>
            <EmployeeProfilePage />
          </ProtectedRoute>
        } />
        <Route path="goals" element={
          <ProtectedRoute allowedRoles={['employee', 'team_lead', 'hr']}>
            <GoalsPage />
          </ProtectedRoute>
        } />
        <Route path="reviews" element={
          <ProtectedRoute allowedRoles={['employee', 'team_lead', 'hr']}>
            <ReviewsPage />
          </ProtectedRoute>
        } />
        <Route path="promotion-requests" element={
          <ProtectedRoute allowedRoles={['employee', 'team_lead', 'hr']}>
            <PromotionRequestsPage />
          </ProtectedRoute>
        } />
        <Route path="calibration" element={
          <ProtectedRoute allowedRoles={['team_lead', 'hr']}>
            <CalibrationPage />
          </ProtectedRoute>
        } />
        <Route path="employees" element={
          <ProtectedRoute allowedRoles={['hr']}>
            <EmployeeManagementPage />
          </ProtectedRoute>
        } />
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" />} />
      <Route path="*" element={<Navigate to="/dashboard" />} />
    </Routes>
  );
};

export default AppRoutes;