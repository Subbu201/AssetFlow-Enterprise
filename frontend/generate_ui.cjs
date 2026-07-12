const fs = require('fs');
const path = require('path');

const generateTablePage = (title, columns, mockData) => {
  return `import React from 'react';
import { Box, Typography, Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';

const mockData = ${JSON.stringify(mockData, null, 2)};

const Page = () => {
  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4" color="textPrimary">
          ${title}
        </Typography>
        <Button variant="contained" color="primary" startIcon={<AddIcon />}>
          Create New
        </Button>
      </Box>
      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              ${columns.map(c => `<TableCell sx={{ fontWeight: 'bold' }}>${c}</TableCell>`).join('\n              ')}
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {mockData.map((row, idx) => (
              <TableRow key={idx}>
                ${columns.map(c => `<TableCell>{row['${c.toLowerCase()}'] || 'Data'}</TableCell>`).join('\n                ')}
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
export default Page;`;
};

const generateFormPage = (title) => {
  return `import React from 'react';
import { Box, Typography, Card, TextField, Button, Grid } from '@mui/material';

const Page = () => {
  return (
    <Box sx={{ maxWidth: 800, margin: '0 auto' }}>
      <Typography variant="h4" color="textPrimary" sx={{ mb: 4 }}>
        ${title}
      </Typography>
      <Card sx={{ p: 4 }}>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6}>
            <TextField label="Field 1" fullWidth />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Field 2" fullWidth />
          </Grid>
          <Grid item xs={12}>
            <TextField label="Description" multiline rows={4} fullWidth />
          </Grid>
          <Grid item xs={12} sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
            <Button variant="outlined">Cancel</Button>
            <Button variant="contained" color="primary">Save</Button>
          </Grid>
        </Grid>
      </Card>
    </Box>
  );
};
export default Page;`;
};

const pages = [
  { path: 'assets/AssetListPage.jsx', content: generateTablePage('Asset Inventory', ['Name', 'Tag', 'Status', 'Condition'], [{name: 'MacBook Pro', tag: 'AST-001', status: 'AVAILABLE', condition: 'NEW'}]) },
  { path: 'assets/AssetCreatePage.jsx', content: generateFormPage('Register New Asset') },
  { path: 'assets/AssetEditPage.jsx', content: generateFormPage('Edit Asset') },
  { path: 'assets/AssetDetailsPage.jsx', content: generateFormPage('Asset Details') },
  { path: 'allocation/AllocationPage.jsx', content: generateTablePage('Allocations', ['Asset', 'Employee', 'Date Assigned', 'Status'], [{asset: 'MacBook Pro', employee: 'John Doe', 'date assigned': '2023-01-01', status: 'ACTIVE'}]) },
  { path: 'allocation/TransferRequestPage.jsx', content: generateFormPage('Request Transfer') },
  { path: 'allocation/ReturnPage.jsx', content: generateFormPage('Return Asset') },
  { path: 'booking/BookingListPage.jsx', content: generateTablePage('Bookings', ['Resource', 'User', 'Start', 'End'], [{resource: 'Conference Room A', user: 'Jane Smith', start: '10:00 AM', end: '11:00 AM'}]) },
  { path: 'booking/BookingCreatePage.jsx', content: generateFormPage('Create Booking') },
  { path: 'booking/BookingCalendarPage.jsx', content: generateFormPage('Booking Calendar') },
  { path: 'maintenance/MaintenanceListPage.jsx', content: generateTablePage('Maintenance Records', ['Asset', 'Technician', 'Date', 'Cost'], [{asset: 'Dell XPS', technician: 'Mike Fix', date: '2023-05-10', cost: '$150'}]) },
  { path: 'maintenance/MaintenanceCreatePage.jsx', content: generateFormPage('Schedule Maintenance') },
  { path: 'maintenance/MaintenanceDetailsPage.jsx', content: generateFormPage('Maintenance Details') },
  { path: 'maintenance/TechnicianWorkflowPage.jsx', content: generateTablePage('Technician Tasks', ['Task', 'Asset', 'Priority', 'Status'], [{task: 'Replace Screen', asset: 'iPhone 13', priority: 'HIGH', status: 'PENDING'}]) },
  { path: 'audit/AuditCycleListPage.jsx', content: generateTablePage('Audit Cycles', ['Name', 'Start Date', 'End Date', 'Status'], [{name: 'Q1 Audit', 'start date': '2023-01-01', 'end date': '2023-03-31', status: 'COMPLETED'}]) },
  { path: 'audit/AuditCreatePage.jsx', content: generateFormPage('Create Audit Cycle') },
  { path: 'audit/AuditVerificationPage.jsx', content: generateFormPage('Verify Assets') },
  { path: 'audit/DiscrepancyReportPage.jsx', content: generateTablePage('Discrepancy Reports', ['Asset', 'Expected Location', 'Actual Location', 'Status'], [{asset: 'Projector', 'expected location': 'Room A', 'actual location': 'Room B', status: 'UNRESOLVED'}]) },
  { path: 'reports/ReportsPage.jsx', content: generateTablePage('System Reports', ['Report Name', 'Generated On', 'Format'], [{'report name': 'Asset Utilization', 'generated on': '2023-10-01', format: 'PDF'}]) },
  { path: 'notifications/NotificationsPage.jsx', content: generateTablePage('Notifications', ['Message', 'Date', 'Type'], [{message: 'Maintenance Due', date: 'Today', type: 'ALERT'}]) },
  { path: 'notifications/ActivityLogsPage.jsx', content: generateTablePage('Activity Logs', ['Action', 'User', 'Timestamp'], [{action: 'Created Asset', user: 'Admin', timestamp: '10:00 AM'}]) },
  { path: 'profile/ProfilePage.jsx', content: generateFormPage('My Profile') }
];

pages.forEach(p => {
  const fullPath = path.join(__dirname, 'src/pages', p.path);
  fs.writeFileSync(fullPath, p.content);
});
console.log('Successfully generated all remaining UI pages!');
