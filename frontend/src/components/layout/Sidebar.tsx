import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Box, Typography, IconButton, Tooltip } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useState } from 'react';
import PersonIcon from '@mui/icons-material/Person';
import FlagIcon from '@mui/icons-material/Flag';
import RateReviewIcon from '@mui/icons-material/RateReview';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import CompareArrowsIcon from '@mui/icons-material/CompareArrows';
import PeopleIcon from '@mui/icons-material/People';
import MenuIcon from '@mui/icons-material/Menu';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';

const drawerWidth = 240;
const drawerWidthCollapsed = 65;

const Sidebar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [collapsed, setCollapsed] = useState(false);
  
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
        width: collapsed ? drawerWidthCollapsed : drawerWidth,
        flexShrink: 0,
        transition: 'width 0.3s ease',
        '& .MuiDrawer-paper': {
          width: collapsed ? drawerWidthCollapsed : drawerWidth,
          boxSizing: 'border-box',
          paddingTop: 2,
          background: '#ffffff',
          transition: 'width 0.3s ease',
          overflowX: 'hidden',
        },
      }}
    >
      <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
        {/* Header */}
        <Box sx={{ px: 2, mb: 2, display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
          {!collapsed && (
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
          )}
          <IconButton 
            onClick={() => setCollapsed(!collapsed)}
            size="small"
            sx={{ 
              ml: collapsed ? 'auto' : 0,
              mr: collapsed ? 'auto' : 0,
            }}
          >
            {collapsed ? <MenuIcon /> : <ChevronLeftIcon />}
          </IconButton>
        </Box>

        {/* Menu Items */}
        <List sx={{ pt: 0 }}>
          {menuItems.map((item) => (
            <ListItem key={item.text} disablePadding>
              <Tooltip title={collapsed ? item.text : ''} placement="right" arrow>
                <ListItemButton
                  selected={location.pathname === item.path}
                  onClick={() => navigate(item.path)}
                  sx={{
                    minHeight: 48,
                    justifyContent: collapsed ? 'center' : 'flex-start',
                    px: collapsed ? 2.5 : 2,
                  }}
                >
                  <ListItemIcon 
                    sx={{ 
                      minWidth: 0,
                      mr: collapsed ? 0 : 3,
                      justifyContent: 'center',
                    }}
                  >
                    {item.icon}
                  </ListItemIcon>
                  {!collapsed && <ListItemText primary={item.text} />}
                </ListItemButton>
              </Tooltip>
            </ListItem>
          ))}
        </List>
      </Box>
    </Drawer>
  );
};

export default Sidebar;