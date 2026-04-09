package com.wealthpro.notifications.repositories;

import com.wealthpro.notifications.entities.Notification;
import com.wealthpro.notifications.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a specific user
    List<Notification> findByUserId(Long userId);

    // Get only unread notifications for a specific user
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    // Mark all notifications as Read for a specific user
    // @Modifying + @Query = custom update query
    // @Transactional = required for update/delete queries
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = :status WHERE n.userId = :userId AND n.status = 'Unread'")
    int markAllAsReadByUserId(Long userId, NotificationStatus status);
}