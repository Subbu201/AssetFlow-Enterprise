package com.assetflow.notification;

import com.assetflow.common.ApiResponse;
import com.assetflow.exception.ForbiddenException;
import com.assetflow.exception.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Notification createNotification(Long recipientUserId, NotificationType type, String title, String message, String referenceType, Long referenceId) {
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        repository.save(notification);
        return notification;
    }

    public List<Notification> getMyNotifications() {
        Long userId = getAuthenticatedUserId();
        return repository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount() {
        Long userId = getAuthenticatedUserId();
        return repository.countByRecipientUserIdAndReadFalse(userId);
    }

    @Transactional
    public Notification markRead(Long id) {
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
        return notification;
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
    public void delete(Long id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        Long userId = getAuthenticatedUserId();
        if (!notification.getRecipientUserId().equals(userId)) {
            throw new ForbiddenException("Cannot delete another user's notification");
        }
        repository.delete(notification);
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("Authentication required");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return Long.parseLong(userDetails.getUsername());
        }
        throw new ForbiddenException("Invalid authentication principal");
    }
}
