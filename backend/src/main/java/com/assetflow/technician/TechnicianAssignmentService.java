package com.assetflow.technician;

import com.assetflow.common.MaintenanceStatus;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.maintenance.entity.MaintenanceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Handles validation and business rules around assigning a technician
 * to an approved maintenance request.
 *
 * Does not own any data — delegates persistence to {@link com.assetflow.maintenance.service.MaintenanceService}.
 * Uses {@link EmployeeLookupGateway} to verify technician existence and status
 * without importing any auth/employee entity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TechnicianAssignmentService {

    private final EmployeeLookupGateway employeeLookupGateway;

    /**
     * Validates that a technician can be assigned to the given maintenance request.
     *
     * Rules:
     * <ol>
     *   <li>technicianUserId must be present (not null).</li>
     *   <li>The referenced employee must exist in the system.</li>
     *   <li>The employee must be ACTIVE.</li>
     *   <li>Technician cannot be assigned before the request is APPROVED.</li>
     *   <li>Technician cannot be changed once repair has started (IN_PROGRESS or later).</li>
     * </ol>
     *
     * @param request          the maintenance request being updated
     * @param technicianUserId the proposed technician user ID
     * @throws BadRequestException  if technicianUserId is null or employee does not exist / is inactive
     * @throws ConflictException    if the state transition is not allowed
     */
    public void validateAndAssign(MaintenanceRequest request, Long technicianUserId) {
        // Rule 1: technicianUserId must be present
        if (technicianUserId == null) {
            throw new BadRequestException("technicianUserId is required.");
        }

        // Rule 4 & 5: State must be APPROVED (can only assign after approval,
        // and cannot change once IN_PROGRESS or RESOLVED)
        MaintenanceStatus currentStatus = request.getStatus();
        if (currentStatus != MaintenanceStatus.APPROVED) {
            if (currentStatus == MaintenanceStatus.PENDING || currentStatus == MaintenanceStatus.REJECTED) {
                throw new ConflictException(
                        "Technician can only be assigned after the request is APPROVED. Current status: " + currentStatus);
            }
            if (currentStatus == MaintenanceStatus.IN_PROGRESS || currentStatus == MaintenanceStatus.RESOLVED) {
                throw new ConflictException(
                        "Technician cannot be changed once repair has started. Current status: " + currentStatus);
            }
        }

        // Rule 2: Employee must exist
        if (!employeeLookupGateway.exists(technicianUserId)) {
            throw new BadRequestException(
                    "No user account found for technicianUserId: " + technicianUserId);
        }

        // Rule 3: Employee must be active
        if (!employeeLookupGateway.isActive(technicianUserId)) {
            String displayName = employeeLookupGateway.getDisplayName(technicianUserId);
            throw new BadRequestException(
                    "User '" + displayName + "' (id=" + technicianUserId + ") is not active and cannot be assigned as a technician.");
        }

        log.info("Technician {} validated for maintenance request {}", technicianUserId, request.getId());
    }
}
