import React, { useState, useEffect } from 'react';
import { Box, Typography, Card, TextField, Button, Grid, CircularProgress, Alert, MenuItem, FormControlLabel, Switch } from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import assetService from '../../services/assetService';
import categoryService from '../../services/categoryService';
import departmentService from '../../services/departmentService';

const AssetEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [categories, setCategories] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    name: '',
    categoryId: '',
    serialNumber: '',
    acquisitionDate: '',
    acquisitionCost: '',
    condition: 'NEW',
    location: '',
    departmentId: '',
    sharedBookable: false,
    manufacturer: '',
    model: '',
    notes: '',
    quantity: 1
  });

  useEffect(() => {
    const fetchAssetAndDependencies = async () => {
      try {
        const [assetRes, catRes, deptRes] = await Promise.all([
          assetService.getAssetById(id),
          categoryService.getAllCategories(),
          departmentService.getAllDepartments()
        ]);
        
        const asset = assetRes.data;
        setFormData({
          name: asset.name || '',
          categoryId: asset.categoryId || '',
          serialNumber: asset.serialNumber || '',
          acquisitionDate: asset.acquisitionDate || '',
          acquisitionCost: asset.acquisitionCost || '',
          condition: asset.condition || 'NEW',
          location: asset.location || '',
          departmentId: asset.departmentId || '',
          sharedBookable: asset.sharedBookable || false,
          manufacturer: asset.manufacturer || '',
          model: asset.model || '',
          notes: asset.notes || '',
          quantity: asset.quantity || 1
        });
        
        setCategories(catRes.data?.content || []);
        setDepartments(deptRes.data?.content || []);
      } catch (err) {
        console.error(err);
        setError('Failed to load asset data or dependencies.');
      } finally {
        setLoading(false);
      }
    };
    fetchAssetAndDependencies();
  }, [id]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    
    try {
      const payload = { ...formData };
      if (!payload.categoryId) throw new Error("Category is required");
      if (!payload.name) throw new Error("Asset name is required");
      if (payload.acquisitionCost) payload.acquisitionCost = parseFloat(payload.acquisitionCost);
      if (payload.quantity) payload.quantity = parseInt(payload.quantity, 10);
      if (payload.departmentId === "") payload.departmentId = null;
      
      await assetService.updateAsset(id, payload);
      navigate(`/assets/${id}`);
    } catch (err) {
      setError(err.message || err.response?.data?.message || 'Failed to update asset');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}><CircularProgress /></Box>;

  return (
    <Box sx={{ maxWidth: 800, margin: '0 auto' }}>
      <Typography variant="h4" color="textPrimary" sx={{ mb: 4 }}>
        Edit Asset
      </Typography>
      
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card sx={{ p: 4 }}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField required label="Asset Name" name="name" value={formData.name} onChange={handleChange} fullWidth />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField required select label="Category" name="categoryId" value={formData.categoryId} onChange={handleChange} fullWidth>
                {categories.map((c) => (
                  <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Serial Number" name="serialNumber" value={formData.serialNumber} onChange={handleChange} fullWidth />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Acquisition Cost" name="acquisitionCost" type="number" inputProps={{ step: "0.01" }} value={formData.acquisitionCost} onChange={handleChange} fullWidth />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Acquisition Date" name="acquisitionDate" type="date" InputLabelProps={{ shrink: true }} value={formData.acquisitionDate} onChange={handleChange} fullWidth />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField select label="Department" name="departmentId" value={formData.departmentId} onChange={handleChange} fullWidth>
                <MenuItem value=""><em>None</em></MenuItem>
                {departments.map((d) => (
                  <MenuItem key={d.id} value={d.id}>{d.name}</MenuItem>
                ))}
              </TextField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Location" name="location" value={formData.location} onChange={handleChange} fullWidth />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Condition" name="condition" value={formData.condition} onChange={handleChange} fullWidth />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Quantity" name="quantity" type="number" value={formData.quantity} onChange={handleChange} fullWidth />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <FormControlLabel
                control={<Switch checked={formData.sharedBookable} onChange={handleChange} name="sharedBookable" color="primary" />}
                label="Make this asset shared/bookable by employees"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField label="Notes" name="notes" multiline rows={3} value={formData.notes} onChange={handleChange} fullWidth />
            </Grid>
            <Grid size={{ xs: 12 }} sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
              <Button variant="outlined" onClick={() => navigate(`/assets/${id}`)}>Cancel</Button>
              <Button type="submit" variant="contained" color="primary" disabled={submitting}>
                {submitting ? <CircularProgress size={24} color="inherit" /> : 'Save Changes'}
              </Button>
            </Grid>
          </Grid>
        </form>
      </Card>
    </Box>
  );
};

export default AssetEditPage;