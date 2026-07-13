import React, { useState, useEffect } from 'react';
import { Box, Typography, Card, Grid, CircularProgress, Alert, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip } from '@mui/material';
import { useAuth } from '../../hooks/useAuth';
import employeeService from '../../services/employeeService';
import departmentService from '../../services/departmentService';

const ProfilePage = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchProfileAndDepartments = async () => {
      try {
        const [profileRes, deptRes] = await Promise.all([
          employeeService.getMyProfile(),
          departmentService.getAllDepartments()
        ]);
        setProfile(profileRes.data);
        setDepartments(deptRes.data?.content || []);
      } catch (err) {
        console.error(err);
        setError('Failed to fetch profile details.');
      } finally {
        setLoading(false);
      }
    };
    fetchProfileAndDepartments();
  }, []);

  const getDepartmentName = (deptId) => {
    if (!deptId) return 'Unassigned';
    const dept = departments.find(d => d.id === deptId);
    return dept ? dept.name : 'Unknown';
  };

  if (loading) return <Box sx={{ p: 4, display: 'flex', justifyContent: 'center' }}><CircularProgress /></Box>;
  if (error) return <Alert severity="error" sx={{ m: 2 }}>{error}</Alert>;
  if (!profile) return <Alert severity="info" sx={{ m: 2 }}>No profile data found.</Alert>;

  return (
    <Box sx={{ maxWidth: 900, margin: '0 auto', p: 2 }}>
      <Typography variant="h4" color="textPrimary" sx={{ mb: 4, fontWeight: 'bold' }}>
        My Profile
      </Typography>

      <Grid container spacing={3}>
        {/* Personal Details Card */}
        <Grid item xs={12} md={6}>
          <Card sx={{ p: 4, height: '100%', bgcolor: 'background.paper', borderRadius: 2, boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}>
            <Typography variant="h6" sx={{ mb: 3, fontWeight: 600, color: 'primary.main' }}>
              Personal Details
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="caption" color="textSecondary">Full Name</Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>{profile.fullName}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="caption" color="textSecondary">Email Address</Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>{profile.email}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="caption" color="textSecondary">Phone Number</Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>{profile.phone || 'N/A'}</Typography>
              </Grid>
            </Grid>
          </Card>
        </Grid>

        {/* Professional Details Card */}
        <Grid item xs={12} md={6}>
          <Card sx={{ p: 4, height: '100%', bgcolor: 'background.paper', borderRadius: 2, boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}>
            <Typography variant="h6" sx={{ mb: 3, fontWeight: 600, color: 'primary.main' }}>
              Organization Profile
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Typography variant="caption" color="textSecondary">Employee Code</Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>{profile.employeeCode || 'N/A'}</Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="caption" color="textSecondary">Current Role</Typography>
                <Box sx={{ mt: 0.5 }}>
                  <Chip label={profile.role || 'EMPLOYEE'} color="secondary" size="small" variant="outlined" />
                </Box>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="caption" color="textSecondary">Department</Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>{getDepartmentName(profile.departmentId)}</Typography>
              </Grid>
              <Grid item xs={6}>
                <Typography variant="caption" color="textSecondary">Designation</Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>{profile.designation || 'N/A'}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="caption" color="textSecondary">Joining Date</Typography>
                <Typography variant="body1" sx={{ fontWeight: 500 }}>{profile.joiningDate || 'N/A'}</Typography>
              </Grid>
            </Grid>
          </Card>
        </Grid>

        {/* Allocated Assets Card */}
        <Grid item xs={12}>
          <Card sx={{ p: 4, mt: 1, bgcolor: 'background.paper', borderRadius: 2, boxShadow: '0 4px 20px rgba(0,0,0,0.08)' }}>
            <Typography variant="h6" sx={{ mb: 3, fontWeight: 600, color: 'primary.main' }}>
              My Allocated Resources
            </Typography>
            {profile.allocatedAssets && profile.allocatedAssets.length > 0 ? (
              <TableContainer component={Paper} elevation={0} sx={{ bgcolor: 'transparent' }}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 'bold', pl: 0 }}>Asset Name</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>Asset Tag</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>Serial Number</TableCell>
                      <TableCell sx={{ fontWeight: 'bold', pr: 0 }} align="right">Status</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {profile.allocatedAssets.map((asset) => (
                      <TableRow key={asset.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                        <TableCell sx={{ pl: 0, fontWeight: 500 }}>{asset.name}</TableCell>
                        <TableCell>{asset.assetTag}</TableCell>
                        <TableCell>{asset.serialNumber || 'N/A'}</TableCell>
                        <TableCell sx={{ pr: 0 }} align="right">
                          <Chip label="ALLOCATED" color="success" size="small" variant="outlined" />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            ) : (
              <Typography variant="body2" color="textSecondary" sx={{ fontStyle: 'italic', py: 2 }}>
                You have no active allocated resources at this time.
              </Typography>
            )}
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ProfilePage;