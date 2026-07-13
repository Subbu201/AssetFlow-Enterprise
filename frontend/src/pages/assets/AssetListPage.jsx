import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, CircularProgress, Alert } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useNavigate } from 'react-router-dom';
import assetService from '../../services/assetService';
import categoryService from '../../services/categoryService';
import { useAuth } from '../../hooks/useAuth';

const AssetListPage = () => {
  const { user } = useAuth();
  const [assets, setAssets] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const isManagerOrAdmin = ['ADMIN', 'ASSET_MANAGER'].includes(user?.role);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [assetRes, catRes] = await Promise.all([
          assetService.getAllAssets({ size: 100 }), 
          categoryService.getAllCategories()
        ]);
        setAssets(assetRes.data?.content || []);
        setCategories(catRes.data?.content || []);
      } catch (err) {
        console.error(err);
        setError('Failed to fetch assets');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const getCategoryName = (id) => {
    const cat = categories.find(c => c.id === id);
    return cat ? cat.name : 'Unknown';
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'AVAILABLE': return 'success';
      case 'ALLOCATED': return 'info';
      case 'UNDER_MAINTENANCE': return 'warning';
      case 'LOST':
      case 'RETIRED':
      case 'DISPOSED': return 'error';
      default: return 'default';
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          Asset Inventory
        </Typography>
        {isManagerOrAdmin && (
          <Button variant="contained" color="primary" startIcon={<AddIcon />} onClick={() => navigate('/assets/create')}>
            Register New Asset
          </Button>
        )}
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Asset Tag</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Name</TableCell>
               <TableCell sx={{ fontWeight: 'bold' }}>Category</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Condition</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Registered By</TableCell>
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
            ) : assets.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                  No assets found.
                </TableCell>
              </TableRow>
            ) : assets.map((asset) => (
              <TableRow key={asset.id}>
                <TableCell>{asset.assetTag}</TableCell>
                <TableCell>{asset.name}</TableCell>
                <TableCell>{getCategoryName(asset.categoryId)}</TableCell>
                <TableCell>{asset.condition || 'N/A'}</TableCell>
                <TableCell>{asset.registeredByUserEmail || 'System'}</TableCell>
                <TableCell>
                  <Chip 
                    label={asset.status} 
                    color={getStatusColor(asset.status)} 
                    size="small" 
                    variant="outlined" 
                  />
                </TableCell>
                <TableCell align="right">
                  <Button size="small" color="primary" onClick={() => navigate(`/assets/${asset.id}`)}>
                    View
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default AssetListPage;