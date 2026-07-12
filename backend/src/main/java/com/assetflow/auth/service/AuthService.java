package com.assetflow.auth.service;

import com.assetflow.auth.dto.*;

public interface AuthService {
    UserResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
