import React, { useState, useEffect } from 'react';
import { Box, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, IconButton, CircularProgress, Alert, Menu, MenuItem } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import { useNavigate } from 'react-router-dom';
import employeeService from '../../services/employeeService';
import departmentService from '../../services/departmentService';

const EmployeeDirectoryPage = () => {
  const navigate = useNavigate();
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [anchorEl, setAnchorEl] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);

  const fetchData = async () => {
    try {
      const [empRes, deptRes] = await Promise.all([
        employeeService.getAllEmployees(),
        departmentService.getAllDepartments()
      ]);
      setEmployees(empRes.data?.content || []);
      setDepartments(deptRes.data?.content || []);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleMenuOpen = (event, user) => {
    setAnchorEl(event.currentTarget);
    setSelectedUser(user);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedUser(null);
  };

  const handleChangeRole = async (newRole) => {
    if (!selectedUser) return;
    try {
      await employeeService.updateRole(selectedUser.id, newRole);
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update role');
    }
    handleMenuClose();
  };

  const getDepartmentName = (deptId) => {
    if (!deptId) return 'Unassigned';
    const dept = departments.find(d => d.id === deptId);
    return dept ? dept.name : 'Unknown';
  };

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          Employee Directory
        </Typography>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Name</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Email</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Role</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Department</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Allocated Assets</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 3 }}>
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : employees.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                  No employees found.
                </TableCell>
              </TableRow>
            ) : employees.map((emp) => (
              <TableRow key={emp.id}>
                <TableCell>{emp.fullName}</TableCell>
                <TableCell>{emp.email}</TableCell>
                <TableCell>
                  <Chip label={emp.role} size="small" color="secondary" variant="outlined" />
                </TableCell>
                <TableCell>{getDepartmentName(emp.departmentId)}</TableCell>
                <TableCell>
                  {emp.allocatedAssets && emp.allocatedAssets.length > 0 ? (
                    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                      {emp.allocatedAssets.map((asset) => (
                        <Chip
                          key={asset.id}
                          label={`${asset.name} (${asset.assetTag})`}
                          size="small"
                          onClick={() => navigate(`/assets/${asset.id}`)}
                          sx={{
                            cursor: 'pointer',
                            backgroundColor: 'rgba(0, 229, 255, 0.08)',
                            color: '#00e5ff',
                            border: '1px solid rgba(0, 229, 255, 0.2)',
                            transition: 'all 0.2s',
                            '&:hover': {
                              backgroundColor: 'rgba(0, 229, 255, 0.16)',
                              transform: 'translateY(-1px)',
                              boxShadow: '0 2px 8px rgba(0, 229, 255, 0.25)',
                            }
                          }}
                        />
                      ))}
                    </Box>
                  ) : (
                    <Typography variant="body2" sx={{ color: 'text.secondary', fontStyle: 'italic' }}>
                      None
                    </Typography>
                  )}
                </TableCell>
                <TableCell>
                  <Chip 
                    label={emp.status} 
                    color={emp.status === 'ACTIVE' ? 'success' : 'error'} 
                    size="small" 
                  />
                </TableCell>
                <TableCell align="right">
                  <IconButton size="small" onClick={(e) => handleMenuOpen(e, emp)}>
                    <MoreVertIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => handleChangeRole('ADMIN')}>Promote to Admin</MenuItem>
        <MenuItem onClick={() => handleChangeRole('ASSET_MANAGER')}>Promote to Asset Manager</MenuItem>
        <MenuItem onClick={() => handleChangeRole('DEPARTMENT_HEAD')}>Promote to Dept Head</MenuItem>
        <MenuItem onClick={() => handleChangeRole('EMPLOYEE')}>Demote to Employee</MenuItem>
      </Menu>
    </Box>
  );
};

export default EmployeeDirectoryPage;
