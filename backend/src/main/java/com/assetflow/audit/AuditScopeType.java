package com.assetflow.audit;

public enum AuditScopeType {
    DEPARTMENT,
    LOCATION;

    public static AuditScopeType fromString(String value) {
        if (value == null) {
            return null;
        }
        return AuditScopeType.valueOf(value.trim().toUpperCase());
    }
}
