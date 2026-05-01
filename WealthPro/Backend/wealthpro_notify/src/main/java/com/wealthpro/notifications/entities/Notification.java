package com.wealthpro.notifications.entities;

import com.wealthpro.notifications.enums.NotificationCategory;
import com.wealthpro.notifications.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    @EqualsAndHashCode.Include
    private Long notificationId;

    // UserID stored as plain Long — no FK to User entity
    // User module (4.1) is a separate microservice
    @Column(name = "UserID", nullable = false)
    private Long userId;

    @Column(name = "Message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "Category", nullable = false, length = 20)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 10)
    private NotificationStatus status;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
}