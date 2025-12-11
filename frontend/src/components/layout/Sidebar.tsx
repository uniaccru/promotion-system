import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Toolbar, Box, Typography } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import PersonIcon from '@mui/icons-material/Person';
import FlagIcon from '@mui/icons-material/Flag';
import RateReviewIcon from '@mui/icons-material/RateReview';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import CompareArrowsIcon from '@mui/icons-material/CompareArrows';
import PeopleIcon from '@mui/icons-material/People';

const drawerWidth = 240;

const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  
  const userRole = user?.role?.toLowerCase() || '';
  const isHR = userRole === 'hr';
  const isTeamLead = userRole === 'team_lead';
  const isEmployee = userRole === 'employee';

  // Define all menu items with role requirements
  const allMenuItems = [
    { text: 'Profile', icon: <PersonIcon />, path: '/dashboard/profile', roles: ['employee', 'team_lead', 'hr'] },
    { text: 'Goals', icon: <FlagIcon />, path: '/dashboard/goals', roles: ['employee', 'team_lead', 'hr'] },
    { text: 'Reviews', icon: <RateReviewIcon />, path: '/dashboard/reviews', roles: ['employee', 'team_lead', 'hr'] },
    { text: 'Promotion Requests', icon: <TrendingUpIcon />, path: '/dashboard/promotion-requests', roles: ['employee', 'team_lead', 'hr'] },
    { text: 'Calibration', icon: <CompareArrowsIcon />, path: '/dashboard/calibration', roles: ['team_lead', 'hr'] },
    { text: 'Employee Management', icon: <PeopleIcon />, path: '/dashboard/employees', roles: ['hr'] },
  ];

  // Filter menu items based on user role
  const menuItems = allMenuItems.filter(item => {
    if (isHR) return item.roles.includes('hr');
    if (isTeamLead) return item.roles.includes('team_lead');
    if (isEmployee) return item.roles.includes('employee');
    return false;
  });

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: drawerWidth,
          boxSizing: 'border-box',
          paddingTop: 16,
          background: '#ffffff',
        },
      }}
    >
      <Toolbar disableGutters sx={{ px: 2, mb: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box
            sx={{
              width: 36,
              height: 36,
              borderRadius: 10,
              background: 'linear-gradient(135deg, rgba(75,44,72,0.95), rgba(26,156,139,0.9))',
              display: 'grid',
              placeItems: 'center',
              color: '#fff',
              fontWeight: 700,
              letterSpacing: 0.2,
            }}
          >
            G
          </Box>
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.1 }}>
              Grading
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Workspace
            </Typography>
          </Box>
        </Box>
      </Toolbar>
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => navigate(item.path)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </Drawer>
  );
};

export default Sidebar;