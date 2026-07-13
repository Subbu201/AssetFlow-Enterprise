import React, { useState, useEffect } from 'react';
import { Box, Typography, Grid, Button, Paper, Divider, CircularProgress, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Inventory2Icon from '@mui/icons-material/Inventory2';
import AssignmentIcon from '@mui/icons-material/Assignment';
import BuildIcon from '@mui/icons-material/Build';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import KpiCard from '../../components/dashboard/KpiCard';
import dashboardService from '../../services/dashboardService';
import { useAuth } from '../../hooks/useAuth';

const DashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const isManagerOrAdmin = ['ADMIN', 'ASSET_MANAGER'].includes(user?.role);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const res = await dashboardService.getDashboardData();
        setData(res.data);
      } catch (err) {
        console.error(err);
        setError('Failed to fetch dashboard data');
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, []);

  if (loading) return <Box sx={{ p: 4, display: 'flex', justifyContent: 'center' }}><CircularProgress /></Box>;
  if (error) return <Alert severity="error">{error}</Alert>;

  const kpis = data?.kpis || {};

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          Dashboard
        </Typography>
        {isManagerOrAdmin && (
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button variant="contained" color="primary" onClick={() => navigate('/assets/create')}>Register Asset</Button>
            <Button variant="outlined" color="primary" onClick={() => navigate('/allocation')}>Allocate Asset</Button>
          </Box>
        )}
      </Box>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <KpiCard title="Assets Available" value={kpis.availableAssets || 0} icon={<Inventory2Icon fontSize="large" />} color="#00E5FF" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <KpiCard title="Assets Allocated" value={kpis.allocatedAssets || 0} icon={<AssignmentIcon fontSize="large" />} color="#64FFDA" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <KpiCard title="Maintenance Today" value={kpis.maintenanceToday || 0} icon={<BuildIcon fontSize="large" />} color="#FF5252" />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <KpiCard title="Pending Transfers" value={kpis.pendingTransfers || 0} icon={<PendingActionsIcon fontSize="large" />} color="#FFB74D" />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Paper sx={{ p: 3, height: '100%', minHeight: 400 }}>
            <Typography variant="h6" gutterBottom>
              Recent Asset Movements
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, height: 300, overflowY: 'auto' }}>
              {data?.upcomingReturns?.length === 0 ? (
                <Typography color="textSecondary" align="center" sx={{ mt: 10 }}>No recent movement history available</Typography>
              ) : data?.upcomingReturns?.map((item, idx) => (
                <Box key={idx} sx={{ p: 2, bgcolor: 'rgba(255,255,255,0.02)', borderRadius: 1 }}>
                  <Typography variant="body1">{item.title}</Typography>
                  <Typography variant="caption" color="textSecondary">{item.subtitle}</Typography>
                </Box>
              ))}
            </Box>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, md: 4 }}>
          <Paper sx={{ p: 3, height: '100%', minHeight: 400 }}>
            <Typography variant="h6" gutterBottom>
              Active Bookings
            </Typography>
            <Divider sx={{ mb: 2 }} />
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, height: 300, overflowY: 'auto' }}>
              {data?.activeBookings?.length === 0 ? (
                <Typography color="textSecondary" align="center" sx={{ mt: 10 }}>No active bookings</Typography>
              ) : data?.activeBookings?.map((item, idx) => (
                <Box key={idx} sx={{ p: 2, bgcolor: 'rgba(255,255,255,0.02)', borderRadius: 1 }}>
                  <Typography variant="body1">{item.title}</Typography>
                  <Typography variant="caption" color="textSecondary">{item.subtitle}</Typography>
                </Box>
              ))}
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;
