const fs = require('fs');
const path = require('path');

const files = [
  'profile/ProfilePage.jsx',
  'maintenance/MaintenanceDetailsPage.jsx',
  'maintenance/MaintenanceCreatePage.jsx',
  'booking/BookingCreatePage.jsx',
  'booking/BookingCalendarPage.jsx',
  'assets/AssetEditPage.jsx',
  'assets/AssetDetailsPage.jsx',
  'audit/AuditVerificationPage.jsx',
  'audit/AuditCreatePage.jsx',
  'allocation/TransferRequestPage.jsx',
  'allocation/ReturnPage.jsx'
];

files.forEach(f => {
  const filePath = path.join(__dirname, 'src/pages', f);
  if (!fs.existsSync(filePath)) return;
  
  let content = fs.readFileSync(filePath, 'utf8');
  
  // Replace <Grid item xs={12} sm={6}> with <Grid size={{ xs: 12, sm: 6 }}>
  content = content.replace(/<Grid\s+item\s+xs=\{12\}\s+sm=\{6\}>/g, '<Grid size={{ xs: 12, sm: 6 }}>');
  
  // Replace <Grid item xs={12}> with <Grid size={{ xs: 12 }}>
  content = content.replace(/<Grid\s+item\s+xs=\{12\}>/g, '<Grid size={{ xs: 12 }}>');
  
  // Replace other variants if any
  content = content.replace(/<Grid\s+item\s+xs=\{12\}\s+sm=\{6\}\s+md=\{3\}>/g, '<Grid size={{ xs: 12, sm: 6, md: 3 }}>');
  content = content.replace(/<Grid\s+item\s+xs=\{12\}\s+md=\{8\}>/g, '<Grid size={{ xs: 12, md: 8 }}>');
  content = content.replace(/<Grid\s+item\s+xs=\{12\}\s+md=\{4\}>/g, '<Grid size={{ xs: 12, md: 4 }}>');
  content = content.replace(/<Grid\s+item\s+xs=\{12\}\s+sx=/g, '<Grid size={{ xs: 12 }} sx=');
  
  fs.writeFileSync(filePath, content, 'utf8');
  console.log(`Updated Grid syntax in ${f}`);
});
console.log('Grid syntax update complete!');
