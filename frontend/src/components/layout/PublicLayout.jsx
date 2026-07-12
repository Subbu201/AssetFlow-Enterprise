import React from 'react';
import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';

const PublicLayout = () => {
  return (
    <Box 
      sx={{ 
        minHeight: '100vh', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center', 
        background: 'radial-gradient(circle at center, #112240 0%, #020C1B 100%)',
        position: 'relative',
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0, left: 0, right: 0, bottom: 0,
          background: 'radial-gradient(circle at top right, rgba(0, 229, 255, 0.1) 0%, transparent 40%), radial-gradient(circle at bottom left, rgba(123, 31, 162, 0.1) 0%, transparent 40%)',
          pointerEvents: 'none',
        }
      }}
    >
      <Box sx={{ zIndex: 1, width: '100%', display: 'flex', justifyContent: 'center' }}>
        <Outlet />
      </Box>
    </Box>
  );
};

export default PublicLayout;
