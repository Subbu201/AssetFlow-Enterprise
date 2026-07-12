import React from 'react';
import { Routes, Route } from 'react-router-dom';

// Layouts
import PublicLayout from '../components/layout/PublicLayout';
import MainLayout from '../components/layout/MainLayout';
import ProtectedRoute from './ProtectedRoute';
import RoleRoute from './RoleRoute';

// Pages
import LoginPage from '../pages/auth/LoginPage';
import SignupPage from '../pages/auth/SignupPage';
import ForgotPasswordPage from '../pages/auth/ForgotPasswordPage';
import DashboardPage from '../pages/dashboard/DashboardPage';
import DepartmentPage from '../pages/organization/DepartmentPage';
import CategoryPage from '../pages/organization/CategoryPage';
import EmployeeDirectoryPage from '../pages/organization/EmployeeDirectoryPage';
import AssetListPage from '../pages/assets/AssetListPage';
import AssetCreatePage from '../pages/assets/AssetCreatePage';
import AssetEditPage from '../pages/assets/AssetEditPage';
import AssetDetailsPage from '../pages/assets/AssetDetailsPage';
import AllocationPage from '../pages/allocation/AllocationPage';
import TransferRequestPage from '../pages/allocation/TransferRequestPage';
import ReturnPage from '../pages/allocation/ReturnPage';
import BookingListPage from '../pages/booking/BookingListPage';
import BookingCreatePage from '../pages/booking/BookingCreatePage';
import BookingCalendarPage from '../pages/booking/BookingCalendarPage';
import MaintenanceListPage from '../pages/maintenance/MaintenanceListPage';
import MaintenanceCreatePage from '../pages/maintenance/MaintenanceCreatePage';
import MaintenanceDetailsPage from '../pages/maintenance/MaintenanceDetailsPage';
import TechnicianWorkflowPage from '../pages/maintenance/TechnicianWorkflowPage';
import AuditCycleListPage from '../pages/audit/AuditCycleListPage';
import AuditCreatePage from '../pages/audit/AuditCreatePage';
import AuditVerificationPage from '../pages/audit/AuditVerificationPage';
import DiscrepancyReportPage from '../pages/audit/DiscrepancyReportPage';
import ReportsPage from '../pages/reports/ReportsPage';
import NotificationsPage from '../pages/notifications/NotificationsPage';
import ActivityLogsPage from '../pages/notifications/ActivityLogsPage';
import ProfilePage from '../pages/profile/ProfilePage';
import NotFoundPage from '../pages/errors/NotFoundPage';
import UnauthorizedPage from '../pages/errors/UnauthorizedPage';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route element={<PublicLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/unauthorized" element={<UnauthorizedPage />} />
      </Route>

      {/* Protected Routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/notifications" element={<NotificationsPage />} />

          {/* Role-based Routes */}
          {/* Admin Routes */}
          <Route element={<RoleRoute allowedRoles={['ADMIN']} />}>
            <Route path="/departments" element={<DepartmentPage />} />
            <Route path="/categories" element={<CategoryPage />} />
            <Route path="/employees" element={<EmployeeDirectoryPage />} />
            <Route path="/audit" element={<AuditCycleListPage />} />
            <Route path="/audit/create" element={<AuditCreatePage />} />
            <Route path="/audit/:id/verify" element={<AuditVerificationPage />} />
            <Route path="/audit/discrepancies" element={<DiscrepancyReportPage />} />
            <Route path="/activity-logs" element={<ActivityLogsPage />} />
          </Route>

          {/* Admin & Asset Manager Routes */}
          <Route element={<RoleRoute allowedRoles={['ADMIN', 'ASSET_MANAGER']} />}>
            <Route path="/assets/create" element={<AssetCreatePage />} />
            <Route path="/assets/:id/edit" element={<AssetEditPage />} />
            <Route path="/allocation" element={<AllocationPage />} />
          </Route>

          {/* General Employee / Department Head / Asset Manager / Admin */}
          <Route path="/assets" element={<AssetListPage />} />
          <Route path="/assets/:id" element={<AssetDetailsPage />} />
          <Route path="/transfer-request" element={<TransferRequestPage />} />
          <Route path="/return" element={<ReturnPage />} />
          <Route path="/booking" element={<BookingListPage />} />
          <Route path="/booking/create" element={<BookingCreatePage />} />
          <Route path="/booking/calendar" element={<BookingCalendarPage />} />
          <Route path="/maintenance" element={<MaintenanceListPage />} />
          <Route path="/maintenance/create" element={<MaintenanceCreatePage />} />
          <Route path="/maintenance/:id" element={<MaintenanceDetailsPage />} />
          <Route path="/technician" element={<TechnicianWorkflowPage />} />
          <Route path="/reports" element={<ReportsPage />} />

        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;
