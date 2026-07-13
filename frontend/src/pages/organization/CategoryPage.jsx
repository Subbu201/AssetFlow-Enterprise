import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, Dialog, DialogTitle, DialogContent, DialogActions, TextField, CircularProgress, Alert } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import categoryService from '../../services/categoryService';

const CategoryPage = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [openModal, setOpenModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ name: '', code: '', description: '', warrantyPeriodMonths: '' });

  const fetchCategories = async () => {
    try {
      const res = await categoryService.getAllCategories();
      setCategories(res.data?.content || []);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch categories');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const handleEditClick = (cat) => {
    setFormData({ 
      name: cat.name, 
      code: cat.code, 
      description: cat.description || '', 
      warrantyPeriodMonths: cat.warrantyPeriodMonths ? cat.warrantyPeriodMonths.toString() : '' 
    });
    setEditingId(cat.id);
    setOpenModal(true);
  };

  const handleCloseModal = () => {
    setOpenModal(false);
    setFormData({ name: '', code: '', description: '', warrantyPeriodMonths: '' });
    setEditingId(null);
  };

  const handleSave = async () => {
    setSubmitting(true);
    try {
      const payload = {
        ...formData,
        warrantyPeriodMonths: formData.warrantyPeriodMonths ? parseInt(formData.warrantyPeriodMonths) : null
      };
      
      if (editingId) {
        await categoryService.updateCategory(editingId, payload);
      } else {
        await categoryService.createCategory(payload);
      }
      handleCloseModal();
      fetchCategories();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save category');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          Asset Categories
        </Typography>
        <Button variant="contained" color="primary" startIcon={<AddIcon />} onClick={() => setOpenModal(true)}>
          Create Category
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Name</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Code</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Description</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Warranty (Months)</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 3 }}>
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : categories.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                  No categories found.
                </TableCell>
              </TableRow>
            ) : categories.map((cat) => (
              <TableRow key={cat.id}>
                <TableCell>{cat.name}</TableCell>
                <TableCell>{cat.code}</TableCell>
                <TableCell>{cat.description}</TableCell>
                <TableCell>{cat.warrantyPeriodMonths || 'N/A'}</TableCell>
                <TableCell>
                  <Chip 
                    label={cat.status} 
                    color={cat.status === 'ACTIVE' ? 'success' : 'error'} 
                    size="small" 
                    variant="outlined" 
                  />
                </TableCell>
                <TableCell align="right">
                  <Button size="small" color="primary" onClick={() => handleEditClick(cat)}>Edit</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={openModal} onClose={handleCloseModal} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Category' : 'Create Category'}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2 }}>
          <TextField 
            label="Name" 
            fullWidth 
            value={formData.name} 
            onChange={(e) => setFormData({...formData, name: e.target.value})} 
          />
          <TextField 
            label="Code" 
            fullWidth 
            value={formData.code} 
            onChange={(e) => setFormData({...formData, code: e.target.value})} 
          />
          <TextField 
            label="Description" 
            fullWidth 
            multiline rows={3}
            value={formData.description} 
            onChange={(e) => setFormData({...formData, description: e.target.value})} 
          />
          <TextField 
            label="Warranty Period (Months)" 
            type="number"
            fullWidth 
            value={formData.warrantyPeriodMonths} 
            onChange={(e) => setFormData({...formData, warrantyPeriodMonths: e.target.value})} 
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={handleCloseModal}>Cancel</Button>
          <Button variant="contained" color="primary" onClick={handleSave} disabled={submitting}>
            {submitting ? <CircularProgress size={24} color="inherit" /> : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CategoryPage;
