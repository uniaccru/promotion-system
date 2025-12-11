import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';
import Sidebar from '@/components/layout/Sidebar';
import Header from '@/components/layout/Header';

const DashboardLayout = () => {
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: '#f6f7fa' }}>
      <Sidebar />
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        <Header />
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            p: { xs: 2, md: 3 },
            background: 'radial-gradient(circle at 10% 20%, rgba(75,44,72,0.04), transparent 25%), radial-gradient(circle at 90% 10%, rgba(26,156,139,0.05), transparent 25%), #f6f7fa',
          }}
        >
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
};

export default DashboardLayout;