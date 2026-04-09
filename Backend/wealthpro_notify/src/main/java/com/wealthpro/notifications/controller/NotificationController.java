package com.wealthpro.notifications.controller;

import com.wealthpro.notifications.dto.request.NotificationRequestDTO;
import com.wealthpro.notifications.dto.request.NotificationStatusUpdateDTO;
import com.wealthpro.notifications.dto.response.NotificationResponseDTO;
import com.wealthpro.notifications.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ─────────────────────────────────────────
    // POST /api/notifications
    // Create a notification
    // Postman: Body → raw → JSON
    // ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(requestDTO)); // 201
    }

    // ─────────────────────────────────────────
    // GET /api/notifications
    // Get all notifications
    // ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications()); // 200
    }

    // ─────────────────────────────────────────
    // GET /api/notifications/{notificationId}
    // Get notification by ID
    // ─────────────────────────────────────────
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService.getNotificationById(notificationId)); // 200
    }

    // ─────────────────────────────────────────
    // GET /api/notifications/user/{userId}
    // Get all notifications for a user
    // ─────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByUserId(userId)); // 200
    }

    // ─────────────────────────────────────────
    // GET /api/notifications/user/{userId}/unread
    // Get only unread notifications for a user
    // ─────────────────────────────────────────
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotificationsByUserId(userId)); // 200
    }

    // ─────────────────────────────────────────
    // PUT /api/notifications/{notificationId}/status
    // Update notification status
    // Postman: Body → raw → JSON
    // { "status": "Read" }
    // ─────────────────────────────────────────
    @PutMapping("/{notificationId}/status")
    public ResponseEntity<NotificationResponseDTO> updateStatus(
            @PathVariable Long notificationId,
            @Valid @RequestBody NotificationStatusUpdateDTO requestDTO) {

        return ResponseEntity.ok(
                notificationService.updateStatus(notificationId, requestDTO)); // 200
    }

    // ─────────────────────────────────────────
    // PUT /api/notifications/user/{userId}/read-all
    // Mark all notifications as Read for a user
    // ─────────────────────────────────────────
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllAsRead(@PathVariable Long userId) {

        int count = notificationService.markAllAsReadByUserId(userId);
        return ResponseEntity.ok(count + " notifications marked as Read"); // 200
    }

    // ─────────────────────────────────────────
    // DELETE /api/notifications/{notificationId}
    // Delete a notification
    // ─────────────────────────────────────────
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable Long notificationId) {

        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok("Notification deleted successfully"); // 200
    }
}