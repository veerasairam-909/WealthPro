package com.wealthpro.notifications.alltests;

import com.wealthpro.notifications.dto.request.NotificationRequestDTO;
import com.wealthpro.notifications.dto.request.NotificationStatusUpdateDTO;
import com.wealthpro.notifications.dto.response.NotificationResponseDTO;
import com.wealthpro.notifications.entities.Notification;
import com.wealthpro.notifications.enums.NotificationCategory;
import com.wealthpro.notifications.enums.NotificationStatus;
import com.wealthpro.notifications.exception.InvalidOperationException;
import com.wealthpro.notifications.exception.ResourceNotFoundException;
import com.wealthpro.notifications.repositories.NotificationRepository;
import com.wealthpro.notifications.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;
    private NotificationRequestDTO requestDTO;
    private NotificationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setNotificationId(1L);
        notification.setUserId(1L);
        notification.setMessage("Your KYC has been verified");
        notification.setCategory(NotificationCategory.Compliance);
        notification.setStatus(NotificationStatus.Unread);
        notification.setCreatedDate(LocalDateTime.now());

        requestDTO = new NotificationRequestDTO();
        requestDTO.setUserId(1L);
        requestDTO.setMessage("Your KYC has been verified");
        requestDTO.setCategory(NotificationCategory.Compliance);

        responseDTO = new NotificationResponseDTO();
        responseDTO.setNotificationId(1L);
        responseDTO.setUserId(1L);
        responseDTO.setMessage("Your KYC has been verified");
        responseDTO.setCategory(NotificationCategory.Compliance);
        responseDTO.setStatus(NotificationStatus.Unread);
        responseDTO.setCreatedDate(LocalDateTime.now());
    }

    // ─────────────────────────────────────────
    // TEST 1: Create notification — success
    // ─────────────────────────────────────────
    @Test
    void testCreateNotification_Success() {
        when(modelMapper.map(requestDTO, Notification.class))
                .thenReturn(notification);
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);
        when(modelMapper.map(notification, NotificationResponseDTO.class))
                .thenReturn(responseDTO);

        NotificationResponseDTO result =
                notificationService.createNotification(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getNotificationId());
        assertEquals(NotificationStatus.Unread, result.getStatus());

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ─────────────────────────────────────────
    // TEST 2: Get all notifications
    // ─────────────────────────────────────────
    @Test
    void testGetAllNotifications_ReturnsList() {
        when(notificationRepository.findAll()).thenReturn(List.of(notification));
        when(modelMapper.map(notification, NotificationResponseDTO.class))
                .thenReturn(responseDTO);

        List<NotificationResponseDTO> result =
                notificationService.getAllNotifications();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(notificationRepository, times(1)).findAll();
    }

    // ─────────────────────────────────────────
    // TEST 3: Get notification by ID — success
    // ─────────────────────────────────────────
    @Test
    void testGetNotificationById_Success() {
        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));
        when(modelMapper.map(notification, NotificationResponseDTO.class))
                .thenReturn(responseDTO);

        NotificationResponseDTO result =
                notificationService.getNotificationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getNotificationId());

        verify(notificationRepository, times(1)).findById(1L);
    }

    // ─────────────────────────────────────────
    // TEST 4: Get notification by ID — not found
    // ─────────────────────────────────────────
    @Test
    void testGetNotificationById_NotFound_ThrowsException() {
        when(notificationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.getNotificationById(999L);
        });
    }

    // ─────────────────────────────────────────
    // TEST 5: Get notifications by userId
    // ─────────────────────────────────────────
    @Test
    void testGetNotificationsByUserId_ReturnsList() {
        when(notificationRepository.findByUserId(1L))
                .thenReturn(List.of(notification));
        when(modelMapper.map(notification, NotificationResponseDTO.class))
                .thenReturn(responseDTO);

        List<NotificationResponseDTO> result =
                notificationService.getNotificationsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(notificationRepository, times(1)).findByUserId(1L);
    }

    // ─────────────────────────────────────────
    // TEST 6: Get unread notifications by userId
    // ─────────────────────────────────────────
    @Test
    void testGetUnreadNotificationsByUserId_ReturnsUnread() {
        when(notificationRepository.findByUserIdAndStatus(1L,
                NotificationStatus.Unread))
                .thenReturn(List.of(notification));
        when(modelMapper.map(notification, NotificationResponseDTO.class))
                .thenReturn(responseDTO);

        List<NotificationResponseDTO> result =
                notificationService.getUnreadNotificationsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NotificationStatus.Unread, result.get(0).getStatus());
    }

    // ─────────────────────────────────────────
    // TEST 7: Update status — Unread to Read
    // ─────────────────────────────────────────
    @Test
    void testUpdateStatus_UnreadToRead_Success() {
        NotificationStatusUpdateDTO statusRequest =
                new NotificationStatusUpdateDTO();
        statusRequest.setStatus(NotificationStatus.Read);

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);
        when(modelMapper.map(notification, NotificationResponseDTO.class))
                .thenReturn(responseDTO);

        NotificationResponseDTO result =
                notificationService.updateStatus(1L, statusRequest);

        assertNotNull(result);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    // ─────────────────────────────────────────
    // TEST 8: Update status — Dismissed → exception
    // ─────────────────────────────────────────
    @Test
    void testUpdateStatus_Dismissed_ThrowsException() {
        notification.setStatus(NotificationStatus.Dismissed);

        NotificationStatusUpdateDTO statusRequest =
                new NotificationStatusUpdateDTO();
        statusRequest.setStatus(NotificationStatus.Read);

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        assertThrows(InvalidOperationException.class, () -> {
            notificationService.updateStatus(1L, statusRequest);
        });

        verify(notificationRepository, never()).save(any());
    }

    // ─────────────────────────────────────────
    // TEST 9: Update status — Read to Unread → exception
    // ─────────────────────────────────────────
    @Test
    void testUpdateStatus_ReadToUnread_ThrowsException() {
        notification.setStatus(NotificationStatus.Read);

        NotificationStatusUpdateDTO statusRequest =
                new NotificationStatusUpdateDTO();
        statusRequest.setStatus(NotificationStatus.Unread);

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        assertThrows(InvalidOperationException.class, () -> {
            notificationService.updateStatus(1L, statusRequest);
        });

        verify(notificationRepository, never()).save(any());
    }

    // ─────────────────────────────────────────
    // TEST 10: Mark all as read by userId
    // ─────────────────────────────────────────
    @Test
    void testMarkAllAsReadByUserId_Success() {
        when(notificationRepository.markAllAsReadByUserId(1L,
                NotificationStatus.Read)).thenReturn(2);

        int count = notificationService.markAllAsReadByUserId(1L);

        assertEquals(2, count);
        verify(notificationRepository, times(1))
                .markAllAsReadByUserId(1L, NotificationStatus.Read);
    }

    // ─────────────────────────────────────────
    // TEST 11: Delete notification — success
    // ─────────────────────────────────────────
    @Test
    void testDeleteNotification_Success() {
        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotification(1L);

        verify(notificationRepository, times(1)).deleteById(1L);
    }

    // ─────────────────────────────────────────
    // TEST 12: Delete notification — not found
    // ─────────────────────────────────────────
    @Test
    void testDeleteNotification_NotFound_ThrowsException() {
        when(notificationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.deleteNotification(999L);
        });

        verify(notificationRepository, never()).deleteById(any());
    }
}