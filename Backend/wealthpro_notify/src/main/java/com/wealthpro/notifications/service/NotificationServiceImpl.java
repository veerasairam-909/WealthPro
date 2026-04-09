package com.wealthpro.notifications.service;    // ─────────────────────────────────────────



        import com.wealthpro.notifications.dto.request.NotificationRequestDTO;
import com.wealthpro.notifications.dto.request.NotificationStatusUpdateDTO;
import com.wealthpro.notifications.dto.response.NotificationResponseDTO;
import com.wealthpro.notifications.entities.Notification;
import com.wealthpro.notifications.enums.NotificationStatus;
import com.wealthpro.notifications.exception.InvalidOperationException;
import com.wealthpro.notifications.exception.ResourceNotFoundException;
import com.wealthpro.notifications.repositories.NotificationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   ModelMapper modelMapper) {
        this.notificationRepository = notificationRepository;
        this.modelMapper = modelMapper;
    }

    // ─────────────────────────────────────────
    // CREATE notification
    // ─────────────────────────────────────────
    @Override
    public NotificationResponseDTO createNotification(NotificationRequestDTO requestDTO) {

        Notification notification = modelMapper.map(requestDTO, Notification.class);

        // Auto set status to Unread on creation
        notification.setStatus(NotificationStatus.Unread);

        // Auto set createdDate to current datetime
        notification.setCreatedDate(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        return modelMapper.map(saved, NotificationResponseDTO.class);
    }

    // ─────────────────────────────────────────
    // GET all notifications
    // ─────────────────────────────────────────
    @Override
    public List<NotificationResponseDTO> getAllNotifications() {
        List<Notification> list = notificationRepository.findAll();
        List<NotificationResponseDTO> result = new ArrayList<NotificationResponseDTO>();
        for (Notification n : list) {
            result.add(modelMapper.map(n, NotificationResponseDTO.class));
        }
        return result;
    }

    // ─────────────────────────────────────────
    // GET notification by ID
    // ─────────────────────────────────────────
    @Override
    public NotificationResponseDTO getNotificationById(Long notificationId) {
        Notification notification = findNotificationOrThrow(notificationId);
        return modelMapper.map(notification, NotificationResponseDTO.class);
    }

    // ─────────────────────────────────────────
    // GET all notifications for a user
    // ─────────────────────────────────────────
    @Override
    public List<NotificationResponseDTO> getNotificationsByUserId(Long userId) {
        List<Notification> list = notificationRepository.findByUserId(userId);
        List<NotificationResponseDTO> result = new ArrayList<NotificationResponseDTO>();
        for (Notification n : list) {
            result.add(modelMapper.map(n, NotificationResponseDTO.class));
        }
        return result;
    }

    // ─────────────────────────────────────────
    // GET only unread notifications for a user
    // ─────────────────────────────────────────
    @Override
    public List<NotificationResponseDTO> getUnreadNotificationsByUserId(Long userId) {
        List<Notification> list =
                notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.Unread);
        List<NotificationResponseDTO> result = new ArrayList<NotificationResponseDTO>();
        for (Notification n : list) {
            result.add(modelMapper.map(n, NotificationResponseDTO.class));
        }
        return result;
    }

    // ─────────────────────────────────────────
    // UPDATE notification status
    // Business rules:
    // Unread    → Read       ✅
    // Unread    → Dismissed  ✅
    // Read      → Dismissed  ✅
    // Read      → Unread     ❌
    // Dismissed → anything   ❌ final state
    // ─────────────────────────────────────────
    @Override
    public NotificationResponseDTO updateStatus(Long notificationId,
                                                NotificationStatusUpdateDTO requestDTO) {
        Notification notification = findNotificationOrThrow(notificationId);

        NotificationStatus currentStatus = notification.getStatus();
        NotificationStatus newStatus = requestDTO.getStatus();

        // Cannot change Dismissed — it is final state
        if (currentStatus == NotificationStatus.Dismissed) {
            throw new InvalidOperationException(
                    "Cannot update a Dismissed notification — it is a final state");
        }

        // Cannot go back from Read to Unread
        if (currentStatus == NotificationStatus.Read
                && newStatus == NotificationStatus.Unread) {
            throw new InvalidOperationException(
                    "Cannot revert status from Read back to Unread");
        }

        notification.setStatus(newStatus);
        Notification updated = notificationRepository.save(notification);
        return modelMapper.map(updated, NotificationResponseDTO.class);
    }

    // ─────────────────────────────────────────
    // MARK ALL as Read for a user
    // ─────────────────────────────────────────
    @Override
    public int markAllAsReadByUserId(Long userId) {
        return notificationRepository
                .markAllAsReadByUserId(userId, NotificationStatus.Read);
    }

    // ─────────────────────────────────────────
    // DELETE notification
    // ─────────────────────────────────────────
    @Override
    public void deleteNotification(Long notificationId) {
        findNotificationOrThrow(notificationId);
        notificationRepository.deleteById(notificationId);
    }

    // PRIVATE HELPER
// ─────────────────────────────────────────
    private Notification findNotificationOrThrow(Long notificationId) {
        Optional<Notification> optional = notificationRepository.findById(notificationId);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException(
                    "Notification not found with ID: " + notificationId);
        }
    }
}