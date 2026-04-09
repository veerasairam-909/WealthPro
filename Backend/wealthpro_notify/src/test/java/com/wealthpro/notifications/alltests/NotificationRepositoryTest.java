package com.wealthpro.notifications.alltests;

import com.wealthpro.notifications.entities.Notification;
import com.wealthpro.notifications.enums.NotificationCategory;
import com.wealthpro.notifications.enums.NotificationStatus;
import com.wealthpro.notifications.repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setUserId(1L);
        notification.setMessage("Your KYC has been verified");
        notification.setCategory(NotificationCategory.Compliance);
        notification.setStatus(NotificationStatus.Unread);
        notification.setCreatedDate(LocalDateTime.now());
    }

    // ─────────────────────────────────────────
    // TEST 1: Save notification
    // ─────────────────────────────────────────
    @Test
    void testSaveNotification_Success() {
        Notification saved = notificationRepository.save(notification);

        assertNotNull(saved);
        assertNotNull(saved.getNotificationId());
        assertEquals("Your KYC has been verified", saved.getMessage());
        assertEquals(NotificationStatus.Unread, saved.getStatus());
        assertEquals(NotificationCategory.Compliance, saved.getCategory());
    }

    // ─────────────────────────────────────────
    // TEST 2: Find by ID — exists
    // ─────────────────────────────────────────
    @Test
    void testFindById_WhenExists_ReturnsNotification() {
        Notification saved = notificationRepository.save(notification);

        Optional<Notification> found = notificationRepository
                .findById(saved.getNotificationId());

        assertTrue(found.isPresent());
        assertEquals("Your KYC has been verified", found.get().getMessage());
    }

    // ─────────────────────────────────────────
    // TEST 3: Find by ID — not found
    // ─────────────────────────────────────────
    @Test
    void testFindById_WhenNotExists_ReturnsEmpty() {
        Optional<Notification> found = notificationRepository.findById(999L);

        assertFalse(found.isPresent());
    }

    // ─────────────────────────────────────────
    // TEST 4: Find by userId
    // ─────────────────────────────────────────
    @Test
    void testFindByUserId_ReturnsNotifications() {
        notificationRepository.save(notification);

        Notification notification2 = new Notification();
        notification2.setUserId(1L);
        notification2.setMessage("Your order has been placed");
        notification2.setCategory(NotificationCategory.Order);
        notification2.setStatus(NotificationStatus.Unread);
        notification2.setCreatedDate(LocalDateTime.now());
        notificationRepository.save(notification2);

        List<Notification> notifications = notificationRepository
                .findByUserId(1L);

        assertEquals(2, notifications.size());
    }

    // ─────────────────────────────────────────
    // TEST 5: Find by userId — no notifications
    // ─────────────────────────────────────────
    @Test
    void testFindByUserId_WhenNone_ReturnsEmpty() {
        List<Notification> notifications = notificationRepository
                .findByUserId(999L);

        assertEquals(0, notifications.size());
    }

    // ─────────────────────────────────────────
    // TEST 6: Find by userId and status — Unread
    // ─────────────────────────────────────────
    @Test
    void testFindByUserIdAndStatus_ReturnsUnread() {
        notificationRepository.save(notification);

        // Save a Read notification for same user
        Notification readNotification = new Notification();
        readNotification.setUserId(1L);
        readNotification.setMessage("Portfolio review done");
        readNotification.setCategory(NotificationCategory.Review);
        readNotification.setStatus(NotificationStatus.Read);
        readNotification.setCreatedDate(LocalDateTime.now());
        notificationRepository.save(readNotification);

        List<Notification> unread = notificationRepository
                .findByUserIdAndStatus(1L, NotificationStatus.Unread);

        assertEquals(1, unread.size());
        assertEquals(NotificationStatus.Unread, unread.get(0).getStatus());
    }

    // ─────────────────────────────────────────
    // TEST 7: Update status
    // ─────────────────────────────────────────
    @Test
    void testUpdateStatus_ToRead() {
        Notification saved = notificationRepository.save(notification);

        saved.setStatus(NotificationStatus.Read);
        Notification updated = notificationRepository.save(saved);

        assertEquals(NotificationStatus.Read, updated.getStatus());
    }

    // ─────────────────────────────────────────
    // TEST 8: Mark all as read by userId
    // ─────────────────────────────────────────
    @Test
    void testMarkAllAsReadByUserId() {
        notificationRepository.save(notification);

        Notification notification2 = new Notification();
        notification2.setUserId(1L);
        notification2.setMessage("Another notification");
        notification2.setCategory(NotificationCategory.Order);
        notification2.setStatus(NotificationStatus.Unread);
        notification2.setCreatedDate(LocalDateTime.now());
        notificationRepository.save(notification2);

        int count = notificationRepository
                .markAllAsReadByUserId(1L, NotificationStatus.Read);

        assertEquals(2, count);

        // Verify all are now Read
        List<Notification> unread = notificationRepository
                .findByUserIdAndStatus(1L, NotificationStatus.Unread);
        assertEquals(0, unread.size());
    }

    // ─────────────────────────────────────────
    // TEST 9: Delete notification
    // ─────────────────────────────────────────
    @Test
    void testDeleteNotification_Success() {
        Notification saved = notificationRepository.save(notification);
        Long id = saved.getNotificationId();

        notificationRepository.deleteById(id);

        Optional<Notification> found = notificationRepository.findById(id);
        assertFalse(found.isPresent());
    }

    // ─────────────────────────────────────────
    // TEST 10: Find all notifications
    // ─────────────────────────────────────────
    @Test
    void testFindAll_ReturnsAllNotifications() {
        notificationRepository.save(notification);

        Notification notification2 = new Notification();
        notification2.setUserId(2L);
        notification2.setMessage("Corporate action alert");
        notification2.setCategory(NotificationCategory.CorporateAction);
        notification2.setStatus(NotificationStatus.Unread);
        notification2.setCreatedDate(LocalDateTime.now());
        notificationRepository.save(notification2);

        List<Notification> all = notificationRepository.findAll();
        assertEquals(2, all.size());
    }
}
