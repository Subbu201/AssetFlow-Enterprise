import React from 'react';
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';

const mockData = [
  {
    "task": "Replace Screen",
    "asset": "iPhone 13",
    "priority": "HIGH",
    "status": "PENDING"
  }
];

const Page = () => {
  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          Technician Tasks
        </Typography>
        <Button variant="contained" color="primary" startIcon={<AddIcon />}>
          Create New
        </Button>
      </Box>
      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Task</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Asset</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Priority</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Status</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {mockData.map((row, idx) => (
              <TableRow key={idx}>
                <TableCell>{row['task'] || 'Data'}</TableCell>
                <TableCell>{row['asset'] || 'Data'}</TableCell>
                <TableCell>{row['priority'] || 'Data'}</TableCell>
                <TableCell>{row['status'] || 'Data'}</TableCell>
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