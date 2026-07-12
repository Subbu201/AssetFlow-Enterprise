import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, CircularProgress, Alert } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import allocationService from '../../services/allocationService';
import assetService from '../../services/assetService';
import employeeService from '../../services/employeeService';
import departmentService from '../../services/departmentService';
import { useAuth } from '../../hooks/useAuth';

const AllocationPage = () => {
  const { user } = useAuth();
  
  const [allocations, setAllocations] = useState([]);
  const [assets, setAssets] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [openModal, setOpenModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    assetId: '',
    employeeId: '',
    departmentId: '',
    allocationDate: '',
    expectedReturnDate: '',
    notes: ''
  });

  const fetchData = async () => {
    try {
      const [allocRes, assetRes, empRes, deptRes] = await Promise.all([
        allocationService.getAllAllocations(),
        assetService.getAllAssets({ status: 'AVAILABLE', size: 100 }),
        employeeService.getAllEmployees(),
        departmentService.getAllDepartments()
      ]);
      setAllocations(allocRes.data || []);
      setAssets(assetRes.data?.content || []);
      setEmployees(empRes.data?.content || []);
      setDepartments(deptRes.data?.content || []);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch allocations data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreate = async () => {
    if (!formData.assetId) {
      alert("Please select an asset.");
      return;
    }
    setSubmitting(true);
    try {
      const payload = {
        ...formData,
        allocatedByUserId: user?.id,
        employeeId: formData.employeeId ? parseInt(formData.employeeId) : null,
        departmentId: formData.departmentId ? parseInt(formData.departmentId) : null,
        assetId: parseInt(formData.assetId)
      };
      
      await allocationService.createAllocation(payload);
      setOpenModal(false);
      setFormData({ assetId: '', employeeId: '', departmentId: '', allocationDate: '', expectedReturnDate: '', notes: '' });
      fetchData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create allocation');
    } finally {
      setSubmitting(false);
    }
  };

  const getAssetName = (id) => {
    const item = assets.find(a => a.id === id);
    return item ? `${item.name} (${item.assetTag})` : `Asset ID: ${id}`;
  };

  const getEmployeeName = (id) => {
    if (!id) return 'N/A';
    const emp = employees.find(e => e.id === id);
    return emp ? `${emp.fullName} (${emp.email})` : `User ID: ${id}`;
  };

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          Asset Allocations
        </Typography>
        <Button variant="contained" color="primary" startIcon={<AddIcon />} onClick={() => setOpenModal(true)}>
          Allocate Asset
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Asset</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Employee (Email)</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Date Assigned</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Expected Return</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : allocations.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                  No allocations found.
                </TableCell>
              </TableRow>
            ) : allocations.map((row) => (
              <TableRow key={row.id}>
                <TableCell>{getAssetName(row.assetId)}</TableCell>
                <TableCell>{getEmployeeName(row.employeeId)}</TableCell>
                <TableCell>{row.allocationDate || 'N/A'}</TableCell>
                <TableCell>{row.expectedReturnDate || 'N/A'}</TableCell>
                <TableCell>
                  <Chip 
                    label={row.status || 'ACTIVE'} 
                    color={row.status === 'ACTIVE' || !row.status ? 'success' : 'warning'} 
                    size="small" 
                    variant="outlined" 
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={openModal} onClose={() => setOpenModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Allocate Asset</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField 
            select 
            required 
            label="Asset" 
            fullWidth 
            value={formData.assetId} 
            onChange={(e) => setFormData({...formData, assetId: e.target.value})} 
          >
            {assets.length === 0 ? (
              <MenuItem disabled value="">No available assets found</MenuItem>
            ) : assets.map((a) => (
              <MenuItem key={a.id} value={a.id}>{a.name} ({a.assetTag})</MenuItem>
            ))}
          </TextField>

          <TextField 
            select 
            label="Assign to Employee (by Email)" 
            fullWidth 
            value={formData.employeeId} 
            onChange={(e) => setFormData({...formData, employeeId: e.target.value})} 
            disabled={!!formData.departmentId}
          >
            <MenuItem value=""><em>None</em></MenuItem>
            {employees.map((e) => (
              <MenuItem key={e.id} value={e.id}>{e.email} ({e.fullName})</MenuItem>
            ))}
          </TextField>

          <TextField 
            select 
            label="Assign to Department (Optional)" 
            fullWidth 
            value={formData.departmentId} 
            onChange={(e) => setFormData({...formData, departmentId: e.target.value})} 
            disabled={!!formData.employeeId}
          >
            <MenuItem value=""><em>None</em></MenuItem>
            {departments.map((d) => (
              <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>
            ))}
          </TextField>

          <TextField 
            label="Allocation Date" 
            type="date" 
            fullWidth 
            InputLabelProps={{ shrink: true }}
            value={formData.allocationDate} 
            onChange={(e) => setFormData({...formData, allocationDate: e.target.value})} 
          />

          <TextField 
            label="Expected Return Date" 
            type="date" 
            fullWidth 
            InputLabelProps={{ shrink: true }}
            value={formData.expectedReturnDate} 
            onChange={(e) => setFormData({...formData, expectedReturnDate: e.target.value})} 
          />

          <TextField 
            label="Notes" 
            fullWidth 
            multiline rows={2}
            value={formData.notes} 
            onChange={(e) => setFormData({...formData, notes: e.target.value})} 
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenModal(false)}>Cancel</Button>
          <Button variant="contained" color="primary" onClick={handleCreate} disabled={submitting}>
            {submitting ? <CircularProgress size={24} color="inherit" /> : 'Allocate'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AllocationPage;