package com.wealthpro.notifications.controller;

import com.wealthpro.notifications.dto.request.NotificationRequestDTO;
import com.wealthpro.notifications.dto.request.NotificationStatusUpdateDTO;
import com.wealthpro.notifications.dto.response.NotificationResponseDTO;
import com.wealthpro.notifications.security.AuthContext;
import com.wealthpro.notifications.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private AuthContext ctx(String roles, Long authClientId) {
        return new AuthContext(null, roles, authClientId);
    }

    private void forbid() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    // ─────────────────────────────────────────
    // POST /api/notifications
    // Create a notification (staff-only)
    // ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationRequestDTO requestDTO,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = ctx(roles, authClientId);
        if (ctx.isClient()) forbid();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(requestDTO)); // 201
    }

    // ─────────────────────────────────────────
    // GET /api/notifications
    // Get all notifications (staff-only)
    // ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications(
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = ctx(roles, authClientId);
        if (ctx.isClient()) forbid();

        return ResponseEntity.ok(notificationService.getAllNotifications()); // 200
    }

    // ─────────────────────────────────────────
    // GET /api/notifications/{notificationId}
    // ─────────────────────────────────────────
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(
            @PathVariable Long notificationId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = ctx(roles, authClientId);
        NotificationResponseDTO notification = notificationService.getNotificationById(notificationId);
        if (ctx.isClient() && !ctx.ownsClient(notification.getUserId())) forbid();

        return ResponseEntity.ok(notification); // 200
    }

    // ─────────────────────────────────────────
    // GET /api/notifications/user/{userId}
    // ─────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = ctx(roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(userId)) forbid();

        return ResponseEntity.ok(
                notificationService.getNotificationsByUserId(userId)); // 200
    }

    // ─────────────────────────────────────────
    // GET /api/notifications/user/{userId}/unread
    // ─────────────────────────────────────────
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @PathVariable Long userId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = ctx(roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(userId)) forbid();

        return ResponseEntity.ok(
                notificationService.getUnreadNotificationsByUserId(userId)); // 200
    }

    // ─────────────────────────────────────────
    // PUT /api/notifications/{notificationId}/status
    // ─────────────────────────────────────────
    @PutMapping("/{notificationId}/status")
    public ResponseEntity<NotificationResponseDTO> updateStatus(
            @PathVariable Long notificationId,
            @Valid @RequestBody NotificationStatusUpdateDTO requestDTO,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = ctx(roles, authClientId);
        NotificationResponseDTO existing = notificationService.getNotificationById(notificationId);
        if (ctx.isClient() && !ctx.ownsClient(existing.getUserId())) forbid();

        return ResponseEntity.ok(
                notificationService.updateStatus(notificationId, requestDTO)); // 200
    }

    // ─────────────────────────────────────────
    // PUT /api/notifications/user/{userId}/read-all
    // ─────────────────────────────────────────
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllAsRead(
            @PathVariable Long userId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = ctx(roles, authClientId);
        if (ctx.isClient() && !ctx.ownsClient(userId)) forbid();

        int count = notificationService.markAllAsReadByUserId(userId);
        return ResponseEntity.ok(count + " notifications marked as Read"); // 200
    }

    // ─────────────────────────────────────────
    // DELETE /api/notifications/{notificationId}
    // ─────────────────────────────────────────
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable Long notificationId,
            @RequestHeader(value = AuthContext.HDR_ROLES, required = false) String roles,
            @RequestHeader(value = AuthContext.HDR_CLIENT_ID, required = false) Long authClientId) {

        AuthContext ctx = ctx(roles, authClientId);
        NotificationResponseDTO existing = notificationService.getNotificationById(notificationId);
        if (ctx.isClient() && !ctx.ownsClient(existing.getUserId())) forbid();

        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok("Notification deleted successfully"); // 200
    }
}
