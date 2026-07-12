import React from 'react';
import { Box, Typography, Card, TextField, Button, Grid } from '@mui/material';

const Page = () => {
  return (
    <Box sx={{ maxWidth: 800, margin: '0 auto' }}>
      <Typography variant="h4" color="textPrimary" sx={{ mb: 4 }}>
        Booking Calendar
      </Typography>
      <Card sx={{ p: 4 }}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField label="Field 1" fullWidth />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField label="Field 2" fullWidth />
          </Grid>
          <Grid size={{ xs: 12 }}>
            <TextField label="Description" multiline rows={4} fullWidth />
          </Grid>
          <Grid size={{ xs: 12 }} sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
            <Button variant="outlined">Cancel</Button>
            <Button variant="contained" color="primary">Save</Button>
          </Grid>
        </Grid>
      </Card>
    </Box>
  );
};
export default Page;