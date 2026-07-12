package com.assetflow.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String email, String plainToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("AssetFlow - Password Reset Request");
        message.setText("You have requested to reset your password.\n\n" +
                "Please use the following token to reset your password:\n" +
                plainToken + "\n\n" +
                "If you did not request this, please ignore this email.");

        mailSender.send(message);
    }
}
