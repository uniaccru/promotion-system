import { useEffect, useState } from 'react';
import { Container, Paper, Typography, Grid, Box, CircularProgress } from '@mui/material';
import { employeeService } from '@/services/employeeService';
import { EmployeeProfile } from '@/types';
import { useAuth } from '@/contexts/AuthContext';

const EmployeeProfilePage = () => {
  const [profile, setProfile] = useState<EmployeeProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    const fetchProfile = async () => {
      if (user?.userId) {
        try {
          const data = await employeeService.getProfile(user.userId);
          setProfile(data);
        } catch (error) {
          console.error('Failed to fetch profile:', error);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchProfile();
  }, [user]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!profile) {
    return <Typography>No profile data available</Typography>;
  }

  return (
    <Container maxWidth="lg">
      <Typography variant="h4" gutterBottom>
        Employee Profile
      </Typography>
      <Paper sx={{ p: 3, mt: 2 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">
              Full Name
            </Typography>
            <Typography variant="body1">{profile.fullName}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">
              Email
            </Typography>
            <Typography variant="body1">{profile.email}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">
              Role
            </Typography>
            <Typography variant="body1">{profile.role}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">
              Department
            </Typography>
            <Typography variant="body1">{profile.department}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">
              Current Grade
            </Typography>
            <Typography variant="body1">{profile.currentGrade}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">
              Last Review Score
            </Typography>
            <Typography variant="body1">{profile.lastScore || 'N/A'}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">
              Grade Changes Count
            </Typography>
            <Typography variant="body1">{profile.gradeChangesCount}</Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="subtitle2" color="textSecondary">
              Hire Date
            </Typography>
            <Typography variant="body1">{profile.hireDate}</Typography>
          </Grid>
        </Grid>
      </Paper>
    </Container>
  );
};

export default EmployeeProfilePage;