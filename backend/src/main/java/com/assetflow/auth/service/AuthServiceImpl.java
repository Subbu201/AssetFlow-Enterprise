package com.assetflow.auth.service;

import com.assetflow.auth.dto.*;
import com.assetflow.auth.entity.PasswordResetToken;
import com.assetflow.auth.entity.UserAccount;
import com.assetflow.auth.repository.PasswordResetTokenRepository;
import com.assetflow.auth.repository.UserRepository;
import com.assetflow.common.RecordStatus;
import com.assetflow.common.Role;
import com.assetflow.exception.BadRequestException;
import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.exception.UnauthorizedException;
import com.assetflow.security.jwt.CustomUserDetails;
import com.assetflow.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.security.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Value("${app.password-reset.expiration-minutes:15}")
    private int resetTokenExpiryMinutes;

    @Override
    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email is already in use");
        }

        UserAccount user = UserAccount.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE)
                .status(RecordStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getStatus() != RecordStatus.ACTIVE) {
            throw new ForbiddenException("Account is inactive");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new ForbiddenException("Account is temporarily locked. Try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            }
            userRepository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(token)
                .expiresIn(jwtService.getExpirationTime())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(user -> {
            tokenRepository.invalidateAllTokensForUser(user.getId());
            
            // Generate 6 digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));
            String tokenHash = passwordEncoder.encode(otp); 
            
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userId(user.getId())
                    .tokenHash(tokenHash)
                    .expiresAt(LocalDateTime.now().plusMinutes(resetTokenExpiryMinutes))
                    .build();
            
            tokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), otp);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        UserAccount user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid reset request"));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password cannot be the same as the old password");
        }

        PasswordResetToken resetToken = tokenRepository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token has expired");
        }

        if (!passwordEncoder.matches(request.getToken(), resetToken.getTokenHash())) {
            throw new BadRequestException("Invalid token");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
