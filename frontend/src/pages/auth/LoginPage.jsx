import React, { useState } from 'react';
import { Box, Card, Typography, TextField, Button, Alert, CircularProgress, Link } from '@mui/material';
import { useNavigate, Link as RouterLink, useLocation } from 'react-router-dom';
import authService from '../../services/authService';
import { useAuth } from '../../hooks/useAuth';

const LoginPage = () => {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  
  const successMessage = location.state?.message;

  const handleChange = (e) => {
    setCredentials({ ...credentials, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await authService.login(credentials);
      login(res.data.accessToken, res.data);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 400, width: '100%', p: 2 }}>
      <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3 }}>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h4" color="primary.main" gutterBottom>
            AssetFlow
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Sign in to access your dashboard
          </Typography>
        </Box>

        {error && <Alert severity="error">{error}</Alert>}
        {successMessage && <Alert severity="success">{successMessage}</Alert>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <TextField
            label="Email Address"
            name="email"
            type="email"
            required
            fullWidth
            value={credentials.email}
            onChange={handleChange}
            autoComplete="email"
          />
          <TextField
            label="Password"
            name="password"
            type="password"
            required
            fullWidth
            value={credentials.password}
            onChange={handleChange}
            autoComplete="current-password"
          />
          <Box sx={{ textAlign: 'right' }}>
            <Link component={RouterLink} to="/forgot-password" variant="body2" color="primary.light">
              Forgot password?
            </Link>
          </Box>
          <Button
            type="submit"
            variant="contained"
            color="primary"
            size="large"
            fullWidth
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} color="inherit" /> : 'Login'}
          </Button>
        </form>

        <Box sx={{ textAlign: 'center', mt: 2 }}>
          <Typography variant="body2" color="textSecondary">
            Don't have an account?{' '}
            <Link component={RouterLink} to="/signup" color="primary.light" sx={{ fontWeight: 600 }}>
              Sign up
            </Link>
          </Typography>
        </Box>
      </Card>
    </Box>
  );
};

export default LoginPage;
