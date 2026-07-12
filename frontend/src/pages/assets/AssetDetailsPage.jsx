import React, { useState, useEffect } from 'react';
import { Box, Typography, Card, Grid, CircularProgress, Alert, Button, Chip } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import assetService from '../../services/assetService';

const AssetDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [asset, setAsset] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAsset = async () => {
      try {
        const res = await assetService.getAssetById(id);
        setAsset(res.data);
      } catch (err) {
        console.error(err);
        setError('Failed to fetch asset details.');
      } finally {
        setLoading(false);
      }
    };
    fetchAsset();
  }, [id]);

  if (loading) return <Box sx={{ p: 4, display: 'flex', justifyContent: 'center' }}><CircularProgress /></Box>;
  if (error) return <Alert severity="error">{error}</Alert>;
  if (!asset) return <Alert severity="info">Asset not found.</Alert>;

  return (
    <Box sx={{ maxWidth: 800, margin: '0 auto' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 4 }}>
        <Typography variant="h4" color="textPrimary">Asset Details: {asset.assetTag}</Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button variant="contained" color="primary" onClick={() => navigate(`/assets/edit/${id}`)}>Edit Asset</Button>
          <Button variant="outlined" onClick={() => navigate('/assets')}>Back to List</Button>
        </Box>
      </Box>
      <Card sx={{ p: 4 }}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="subtitle2" color="textSecondary">Name</Typography>
            <Typography variant="body1">{asset.name}</Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="subtitle2" color="textSecondary">Status</Typography>
            <Chip label={asset.status} size="small" color="primary" />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="subtitle2" color="textSecondary">Serial Number</Typography>
            <Typography variant="body1">{asset.serialNumber || 'N/A'}</Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="subtitle2" color="textSecondary">Condition</Typography>
            <Typography variant="body1">{asset.condition || 'N/A'}</Typography>
          </Grid>
          <Grid size={{ xs: 12 }}>
            <Typography variant="subtitle2" color="textSecondary">Notes</Typography>
            <Typography variant="body1">{asset.notes || 'No notes'}</Typography>
          </Grid>
        </Grid>
      </Card>
    </Box>
  );
};

export default AssetDetailsPage;