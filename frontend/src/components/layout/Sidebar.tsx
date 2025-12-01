import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Toolbar } from '@mui/material';
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
        },
      }}
    >
      <Toolbar />
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