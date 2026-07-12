package com.assetflow.technician;

/**
 * Gateway interface isolating the technician module from Member 1's
 * EmployeeProfile / UserAccount services.
 * The integration leader will provide a real implementation once
 * Member 1's branch is merged.
 */
public interface EmployeeLookupGateway {

    /**
     * Returns true if an active user account exists with the given userId.
     *
     * @param userId the UserAccount primary key
     * @return true when the user exists
     */
    boolean exists(Long userId);

    /**
     * Returns true when the user's account status is ACTIVE.
     *
     * @param userId the UserAccount primary key
     * @return true when active
     */
    boolean isActive(Long userId);

    /**
     * Returns a human-readable display name (full name or email) for the user.
     *
     * @param userId the UserAccount primary key
     * @return display name string, never null
     */
    String getDisplayName(Long userId);
}
