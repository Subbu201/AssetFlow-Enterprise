import React from 'react';
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';

const mockData = [
  {
    "asset": "Dell XPS",
    "technician": "Mike Fix",
    "date": "2023-05-10",
    "cost": "$150"
  }
];

const Page = () => {
  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          Maintenance Records
        </Typography>
        <Button variant="contained" color="primary" startIcon={<AddIcon />}>
          Create New
        </Button>
      </Box>
      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Asset</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Technician</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Date</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Cost</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {mockData.map((row, idx) => (
              <TableRow key={idx}>
                <TableCell>{row['asset'] || 'Data'}</TableCell>
                <TableCell>{row['technician'] || 'Data'}</TableCell>
                <TableCell>{row['date'] || 'Data'}</TableCell>
                <TableCell>{row['cost'] || 'Data'}</TableCell>
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