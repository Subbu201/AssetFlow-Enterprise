package com.assetflow.notification;

import com.assetflow.common.ApiResponse;
import com.assetflow.notification.dto.CreateNotificationRequest;
import com.assetflow.notification.dto.NotificationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<NotificationResponse> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        return ApiResponse.success("Notification created", service.createNotification(request));
    }

    @GetMapping("/me")
    public ApiResponse<List<NotificationResponse>> getMyNotifications() {
        return ApiResponse.success("Notifications returned", service.getMyNotifications());
    }

    @GetMapping("/me/unread-count")
    public ApiResponse<Long> getUnreadCount() {
        return ApiResponse.success("Unread notification count returned", service.getUnreadCount());
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable Long id) {
        return ApiResponse.success("Notification marked read", service.markRead(id));
    }

    @PatchMapping("/read-all")
    public ApiResponse<String> readAll() {
        service.readAll();
        return ApiResponse.success("All notifications marked as read", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        service.deleteNotification(id);
        return ApiResponse.success("Notification deleted", null);
    }
}
