import React, { useState } from 'react';
import { Box, Card, Typography, TextField, Button, Alert, CircularProgress, Link } from '@mui/material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import authService from '../../services/authService';
import { useAuth } from '../../hooks/useAuth';

const SignupPage = () => {
  const [formData, setFormData] = useState({ fullName: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        confirmPassword: formData.confirmPassword
      };
      
      const res = await authService.signup(payload);
      // Redirect to login page after successful signup since it doesn't return a token
      navigate('/login', { state: { message: 'Registration successful! Please login.' } });
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 450, width: '100%', p: 2 }}>
      <Card sx={{ p: 4, display: 'flex', flexDirection: 'column', gap: 3 }}>
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h4" color="primary.main" gutterBottom>
            Create Account
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Join AssetFlow as an Employee
          </Typography>
        </Box>

        {error && <Alert severity="error">{error}</Alert>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <TextField
            label="Full Name"
            name="fullName"
            required
            fullWidth
            value={formData.fullName}
            onChange={handleChange}
            autoComplete="name"
          />
          <TextField
            label="Email Address"
            name="email"
            type="email"
            required
            fullWidth
            value={formData.email}
            onChange={handleChange}
            autoComplete="email"
          />
          <TextField
            label="Password"
            name="password"
            type="password"
            required
            fullWidth
            value={formData.password}
            onChange={handleChange}
            autoComplete="new-password"
          />
          <TextField
            label="Confirm Password"
            name="confirmPassword"
            type="password"
            required
            fullWidth
            value={formData.confirmPassword}
            onChange={handleChange}
            autoComplete="new-password"
          />
          <Button
            type="submit"
            variant="contained"
            color="primary"
            size="large"
            fullWidth
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} color="inherit" /> : 'Sign Up'}
          </Button>
        </form>

        <Box sx={{ textAlign: 'center', mt: 2 }}>
          <Typography variant="body2" color="textSecondary">
            Already have an account?{' '}
            <Link component={RouterLink} to="/login" color="primary.light" sx={{ fontWeight: 600 }}>
              Login
            </Link>
          </Typography>
        </Box>
      </Card>
    </Box>
  );
};

export default SignupPage;
