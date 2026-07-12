package com.assetflow.auth.dto;

import com.assetflow.common.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private long expiresIn;
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
}
