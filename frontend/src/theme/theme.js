import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#00E5FF', // Neon Cyan / Magic Blue
      light: '#64FFDA',
      dark: '#00B8D4',
      contrastText: '#0A192F',
    },
    secondary: {
      main: '#7B1FA2', // Deep Purple / Magic Violet
      light: '#AB47BC',
      dark: '#4A148C',
    },
    background: {
      default: '#020C1B', // Deep Space Blue
      paper: '#112240', // Glassmorphic Card Blue
    },
    text: {
      primary: '#E6F1FF',
      secondary: '#8892B0',
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 700,
      letterSpacing: '0.5px',
    },
    h6: {
      fontWeight: 600,
    }
  },
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
          padding: '8px 24px',
          transition: 'all 0.3s ease-in-out',
          '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: '0 0 15px rgba(0, 229, 255, 0.4)', // Magic Glow
          },
        },
        containedPrimary: {
          background: 'linear-gradient(45deg, #00B8D4 30%, #00E5FF 90%)',
          boxShadow: '0 4px 10px rgba(0, 229, 255, 0.2)',
        }
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundColor: 'rgba(17, 34, 64, 0.7)',
          backdropFilter: 'blur(10px)',
          border: '1px solid rgba(100, 255, 218, 0.1)',
          transition: 'all 0.3s ease-in-out',
          '&:hover': {
            transform: 'translateY(-4px)',
            boxShadow: '0 8px 30px rgba(0, 0, 0, 0.5), 0 0 15px rgba(0, 229, 255, 0.1)',
            border: '1px solid rgba(100, 255, 218, 0.3)',
          },
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            backgroundColor: 'rgba(2, 12, 27, 0.5)',
            transition: 'all 0.3s ease',
            '& fieldset': {
              borderColor: 'rgba(136, 146, 176, 0.2)',
            },
            '&:hover fieldset': {
              borderColor: 'rgba(100, 255, 218, 0.5)',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#00E5FF',
              boxShadow: '0 0 10px rgba(0, 229, 255, 0.2)',
            },
          },
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '#0A192F',
          borderRight: '1px solid rgba(100, 255, 218, 0.1)',
        }
      }
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: 'rgba(10, 25, 47, 0.8)',
          backdropFilter: 'blur(12px)',
          boxShadow: 'none',
          borderBottom: '1px solid rgba(100, 255, 218, 0.1)',
        }
      }
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          transition: 'background-color 0.2s ease',
          '&:hover': {
            backgroundColor: 'rgba(100, 255, 218, 0.05) !important',
          },
        },
      },
    },
  },
});

export default theme;
