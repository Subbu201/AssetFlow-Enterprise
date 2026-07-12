package com.assetflow.technician;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op stub for {@link EmployeeLookupGateway}.
 *
 * Activated automatically whenever no real implementation is present in the
 * Spring context (dev, default, and test profiles).  Once Member 1 provides
 * a concrete {@link EmployeeLookupGateway} bean backed by {@code UserAccount},
 * this stub steps aside automatically via {@code @ConditionalOnMissingBean} in config.
 *
 * Behaviour in dev/default profile:
 * <ul>
 *   <li>{@link #exists(Long)} always returns {@code true}</li>
 *   <li>{@link #isActive(Long)} always returns {@code true}</li>
 *   <li>{@link #getDisplayName(Long)} returns {@code "User-<id>"}</li>
 * </ul>
 */
@Slf4j
public class StubEmployeeLookupGateway implements EmployeeLookupGateway {

    @Override
    public boolean exists(Long userId) {
        log.debug("[STUB] EmployeeLookupGateway.exists({}) → true (stub)", userId);
        return true;
    }

    @Override
    public boolean isActive(Long userId) {
        log.debug("[STUB] EmployeeLookupGateway.isActive({}) → true (stub)", userId);
        return true;
    }

    @Override
    public String getDisplayName(Long userId) {
        return "User-" + userId;
    }
}
