import { createTheme } from '@mui/material/styles';
import { alpha } from '@mui/material';

const primaryPlum = '#4B2C48';
const accentTeal = '#1A9C8B';
const warmGray = '#F6F7FA';
const borderGray = '#E5E7EB';
const textMain = '#1F2933';
const textMuted = '#4B5563';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: primaryPlum,
      light: '#6A4C66',
      dark: '#2E1630',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: accentTeal,
      light: '#37B7A8',
      dark: '#0F7B6F',
      contrastText: '#FFFFFF',
    },
    background: {
      default: warmGray,
      paper: '#FFFFFF',
    },
    text: {
      primary: textMain,
      secondary: textMuted,
    },
    divider: borderGray,
    success: { main: '#2F9E44' },
    warning: { main: '#F59E0B' },
    error: { main: '#D64545' },
    info: { main: '#3B82F6' },
  },
  typography: {
    fontFamily: '"Inter", "Manrope", "Segoe UI", Arial, sans-serif',
    h4: { fontWeight: 600 },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
    button: { fontWeight: 600, textTransform: 'none', letterSpacing: 0.1 },
  },
  shape: {
    borderRadius: 14,
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: warmGray,
          color: textMain,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#FFFFFF',
          color: textMain,
          boxShadow: '0 8px 24px rgba(0,0,0,0.05)',
          borderBottom: `1px solid ${borderGray}`,
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '#FFFFFF',
          borderRight: `1px solid ${borderGray}`,
        },
      },
    },
    MuiToolbar: {
      styleOverrides: {
        root: {
          minHeight: 64,
          '@media (min-width:600px)': {
            minHeight: 72,
          },
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: 'none',
          transition: 'all 180ms ease',
        },
        contained: {
          boxShadow: '0 12px 28px rgba(75,44,72,0.18)',
          '&:hover': {
            boxShadow: '0 14px 32px rgba(75,44,72,0.22)',
          },
        },
        outlined: {
          borderColor: alpha(primaryPlum, 0.25),
          '&:hover': {
            borderColor: primaryPlum,
            backgroundColor: alpha(primaryPlum, 0.05),
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          border: `1px solid ${borderGray}`,
          boxShadow: '0 10px 30px rgba(0,0,0,0.04)',
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: {
          height: 3,
          borderRadius: 3,
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
          letterSpacing: 0.1,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: '#F3F4F6',
          '& .MuiTableCell-root': {
            fontWeight: 600,
            color: textMuted,
          },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderColor: borderGray,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          fontWeight: 600,
          letterSpacing: 0.1,
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 18,
          boxShadow: '0 18px 48px rgba(0,0,0,0.12)',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          marginInline: 10,
          marginBlock: 4,
          transition: 'background-color 160ms ease, transform 160ms ease',
          '&.Mui-selected': {
            backgroundColor: alpha(primaryPlum, 0.08),
            color: primaryPlum,
            '& .MuiListItemIcon-root': {
              color: primaryPlum,
            },
          },
          '&:hover': {
            backgroundColor: alpha(primaryPlum, 0.06),
            transform: 'translateY(-1px)',
          },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: {
        root: {
          color: textMuted,
        },
      },
    },
    MuiContainer: {
      defaultProps: {
        maxWidth: 'lg',
      },
    },
  },
});

export default theme;