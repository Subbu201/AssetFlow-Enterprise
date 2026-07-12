import React from 'react';
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';

const mockData = [
  {
    "resource": "Conference Room A",
    "user": "Jane Smith",
    "start": "10:00 AM",
    "end": "11:00 AM"
  }
];

const Page = () => {
  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          Bookings
        </Typography>
        <Button variant="contained" color="primary" startIcon={<AddIcon />}>
          Create New
        </Button>
      </Box>
      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Resource</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>User</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Start</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>End</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {mockData.map((row, idx) => (
              <TableRow key={idx}>
                <TableCell>{row['resource'] || 'Data'}</TableCell>
                <TableCell>{row['user'] || 'Data'}</TableCell>
                <TableCell>{row['start'] || 'Data'}</TableCell>
                <TableCell>{row['end'] || 'Data'}</TableCell>
                <TableCell align="right">
                  <Button size="small" color="primary">View</Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};
export default Page;