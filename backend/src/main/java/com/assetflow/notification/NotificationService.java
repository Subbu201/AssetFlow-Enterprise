package com.assetflow.notification;

import com.assetflow.exception.ForbiddenException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.notification.dto.CreateNotificationRequest;
import com.assetflow.notification.dto.NotificationResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setRecipientUserId(request.getRecipientUserId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setReferenceType(request.getReferenceType());
        notification.setReferenceId(request.getReferenceId());
        notification.setRead(false);
        repository.save(notification);
        return toResponse(notification);
    }

    public List<NotificationResponse> getMyNotifications() {
        Long userId = getAuthenticatedUserId();
        return repository.findByRecipientUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount() {
        Long userId = getAuthenticatedUserId();
        return repository.countByRecipientUserIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        Long userId = getAuthenticatedUserId();
        if (!notification.getRecipientUserId().equals(userId)) {
            throw new ForbiddenException("Cannot mark another user's notification");
        }
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            repository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public void readAll() {
        Long userId = getAuthenticatedUserId();
        repository.findByRecipientUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> !n.isRead())
                .forEach(n -> {
                    n.setRead(true);
                    n.setReadAt(LocalDateTime.now());
                    repository.save(n);
                });
    }

    @Transactional
    public void deleteNotification(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        Long userId = getAuthenticatedUserId();
        if (!notification.getRecipientUserId().equals(userId)) {
            throw new ForbiddenException("Cannot delete another user's notification");
        }
        repository.delete(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientUserId(notification.getRecipientUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException e) {
                throw new ForbiddenException("Invalid authentication principal");
            }
        }
        throw new ForbiddenException("Invalid authentication principal");
    }
}
