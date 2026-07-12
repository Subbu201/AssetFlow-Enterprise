package com.assetflow.auth.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String plainToken);
}
