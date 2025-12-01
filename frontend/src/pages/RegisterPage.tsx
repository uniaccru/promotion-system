import { useState, useEffect } from 'react';
import { Container, Paper, TextField, Button, Typography, Box, Link, MenuItem, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { gradeService } from '@/services/gradeService';
import { authService } from '@/services/authService';
import { Grade } from '@/types';

const DEPARTMENTS = [
  'Engineering',
  'Product',
  'Design',
  'Marketing',
  'Sales',
  'HR',
  'Finance',
  'Operations',
  'Customer Support',
  'Data Science',
  'QA',
];

const RegisterPage = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    fullName: '',
    email: '',
    role: 'employee',
    hireDate: new Date().toISOString().split('T')[0], // Today's date by default
    department: '',
    reviewPeriod: 'Q4-2024',
    initialGradeId: '',
  });
  const [grades, setGrades] = useState<Grade[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchGrades();
  }, []);

  const fetchGrades = async () => {
    try {
      const data = await gradeService.getAllGrades();
      setGrades(data);
    } catch (error) {
      console.error('Failed to fetch grades:', error);
      setError('Failed to load grades');
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    
    try {
      // Convert initialGradeId to number
      const dataToSubmit = {
        ...formData,
        initialGradeId: Number(formData.initialGradeId),
      };
      
      // Call API directly without login
      await authService.register(dataToSubmit);
      
      // Show success message and reset form
      setSuccess(`Employee ${formData.fullName} registered successfully!`);
      setFormData({
        username: '',
        password: '',
        fullName: '',
        email: '',
        role: 'employee',
        hireDate: new Date().toISOString().split('T')[0],
        department: '',
        reviewPeriod: 'Q4-2024',
        initialGradeId: '',
      });
      
      // If HR, stay on page. If not authenticated, redirect to login
      if (!user) {
        setTimeout(() => navigate('/login'), 2000);
      }
    } catch (err: any) {
      console.error('Registration error:', err);
      setError(err.response?.data?.message || 'Registration failed. Please check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  // Check if user is logged in but not HR (block non-HR authenticated users)
  const userRole = user?.role?.toLowerCase() || '';
  const isAuthenticatedNonHR = user && userRole !== 'hr';
  
  if (isAuthenticatedNonHR) {
    return (
      <Container maxWidth="sm">
        <Box sx={{ mt: 8 }}>
          <Alert severity="error">
            Only HR can register new employees. Please contact your HR department.
          </Alert>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 8 }}>
        <Paper elevation={3} sx={{ p: 4 }}>
          <Typography variant="h4" align="center" gutterBottom>
            Register New Employee
          </Typography>
          <Typography variant="body2" align="center" color="text.secondary" sx={{ mb: 3 }}>
            HR Only - Create a new employee account
          </Typography>
          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Username"
              name="username"
              margin="normal"
              value={formData.username}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Password"
              name="password"
              type="password"
              margin="normal"
              value={formData.password}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Full Name"
              name="fullName"
              margin="normal"
              value={formData.fullName}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              label="Email"
              name="email"
              type="email"
              margin="normal"
              value={formData.email}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              select
              label="Role"
              name="role"
              margin="normal"
              value={formData.role}
              onChange={handleChange}
              required
            >
              <MenuItem value="employee">Employee</MenuItem>
              <MenuItem value="team_lead">Team Lead</MenuItem>
              <MenuItem value="hr">HR</MenuItem>
            </TextField>
            <TextField
              fullWidth
              label="Hire Date"
              name="hireDate"
              type="date"
              margin="normal"
              InputLabelProps={{ shrink: true }}
              value={formData.hireDate}
              onChange={handleChange}
              required
            />
            <TextField
              fullWidth
              select
              label="Department"
              name="department"
              margin="normal"
              value={formData.department}
              onChange={handleChange}
              required
            >
              {DEPARTMENTS.map((dept) => (
                <MenuItem key={dept} value={dept}>
                  {dept}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              fullWidth
              select
              label="Current Grade"
              name="initialGradeId"
              margin="normal"
              value={formData.initialGradeId}
              onChange={handleChange}
              required
              helperText="Select the initial grade for this employee"
            >
              {grades.map((grade) => (
                <MenuItem key={grade.id} value={grade.id}>
                  {grade.name}
                </MenuItem>
              ))}
            </TextField>
            {success && (
              <Alert severity="success" sx={{ mt: 2 }}>
                {success}
              </Alert>
            )}
            {error && (
              <Alert severity="error" sx={{ mt: 2 }}>
                {error}
              </Alert>
            )}
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? 'Registering...' : 'Register New Employee'}
            </Button>
            <Box sx={{ textAlign: 'center' }}>
              {user ? (
                <Link href="/dashboard/employees" underline="hover">
                  Back to Employee Management
                </Link>
              ) : (
                <Link href="/login" underline="hover">
                  Already have an account? Login
                </Link>
              )}
            </Box>
          </form>
        </Paper>
      </Box>
    </Container>
  );
};

export default RegisterPage;