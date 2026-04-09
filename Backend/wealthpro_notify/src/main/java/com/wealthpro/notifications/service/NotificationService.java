package com.wealthpro.notifications.service;

import com.wealthpro.notifications.dto.request.NotificationRequestDTO;
import com.wealthpro.notifications.dto.request.NotificationStatusUpdateDTO;
import com.wealthpro.notifications.dto.response.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {

    NotificationResponseDTO createNotification(NotificationRequestDTO requestDTO);

    List<NotificationResponseDTO> getAllNotifications();

    NotificationResponseDTO getNotificationById(Long notificationId);

    List<NotificationResponseDTO> getNotificationsByUserId(Long userId);

    List<NotificationResponseDTO> getUnreadNotificationsByUserId(Long userId);

    NotificationResponseDTO updateStatus(Long notificationId,
                                         NotificationStatusUpdateDTO requestDTO);

    int markAllAsReadByUserId(Long userId);

    void deleteNotification(Long notificationId);
}