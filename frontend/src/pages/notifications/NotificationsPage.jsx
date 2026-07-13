import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, IconButton, CircularProgress, Alert, Tooltip } from '@mui/material';
import CheckIcon from '@mui/icons-material/Check';
import DeleteIcon from '@mui/icons-material/Delete';
import MarkEmailReadIcon from '@mui/icons-material/MarkEmailRead';
import notificationService from '../../services/notificationService';

const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchNotifications = async () => {
    try {
      const res = await notificationService.getMyNotifications();
      setNotifications(res.data || []);
    } catch (err) {
      console.error(err);
      setError('Failed to fetch notifications.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkRead = async (id) => {
    try {
      await notificationService.markRead(id);
      fetchNotifications();
    } catch (err) {
      alert('Failed to mark notification as read');
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllRead();
      fetchNotifications();
    } catch (err) {
      alert('Failed to mark all notifications as read');
    }
  };

  const handleDelete = async (id) => {
    try {
      await notificationService.deleteNotification(id);
      fetchNotifications();
    } catch (err) {
      alert('Failed to delete notification');
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'ASSET_ASSIGNED': return 'success';
      case 'TRANSFER_APPROVED':
      case 'BOOKING_CONFIRMED':
      case 'MAINTENANCE_APPROVED': return 'info';
      case 'TRANSFER_REJECTED':
      case 'BOOKING_CANCELLED':
      case 'MAINTENANCE_REJECTED':
      case 'OVERDUE_RETURN': return 'error';
      default: return 'default';
    }
  };

  if (loading) return <Box sx={{ p: 4, display: 'flex', justifyContent: 'center' }}><CircularProgress /></Box>;

  return (
    <Box sx={{ p: 1 }}>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary" sx={{ fontWeight: 'bold' }}>
          Notifications
        </Typography>
        {notifications.some(n => !n.read) && (
          <Button 
            variant="outlined" 
            color="primary" 
            startIcon={<MarkEmailReadIcon />} 
            onClick={handleMarkAllRead}
          >
            Mark All as Read
          </Button>
        )}
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper', borderRadius: 2, boxShadow: '0 4px 20px rgba(0,0,0,0.05)' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Title</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Message</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Type</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {notifications.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 6, color: 'text.secondary', fontStyle: 'italic' }}>
                  No notifications found.
                </TableCell>
              </TableRow>
            ) : notifications.map((row) => (
              <TableRow 
                key={row.id} 
                sx={{ 
                  bgcolor: row.read ? 'transparent' : 'rgba(25, 118, 210, 0.04)',
                  transition: 'background-color 0.2s'
                }}
              >
                <TableCell sx={{ fontWeight: row.read ? 400 : 600 }}>{row.title}</TableCell>
                <TableCell>{row.message}</TableCell>
                <TableCell>
                  <Chip 
                    label={row.type ? row.type.replace('_', ' ') : 'GENERAL'} 
                    size="small" 
                    color={getTypeColor(row.type)}
                    variant="outlined" 
                  />
                </TableCell>
                <TableCell>
                  <Chip 
                    label={row.read ? 'Read' : 'Unread'} 
                    size="small" 
                    color={row.read ? 'default' : 'primary'} 
                  />
                </TableCell>
                <TableCell align="right">
                  <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
                    {!row.read && (
                      <Tooltip title="Mark as Read">
                        <IconButton size="small" color="primary" onClick={() => handleMarkRead(row.id)}>
                          <CheckIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    )}
                    <Tooltip title="Delete">
                      <IconButton size="small" color="error" onClick={() => handleDelete(row.id)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default NotificationsPage;