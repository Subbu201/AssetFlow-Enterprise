package com.assetflow.auth.dto;

import com.assetflow.common.RecordStatus;
import com.assetflow.common.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private RecordStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
