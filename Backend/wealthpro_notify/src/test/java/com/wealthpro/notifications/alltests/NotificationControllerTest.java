package com.wealthpro.notifications.alltests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealthpro.notifications.controller.NotificationController;
import com.wealthpro.notifications.dto.request.NotificationRequestDTO;
import com.wealthpro.notifications.dto.request.NotificationStatusUpdateDTO;
import com.wealthpro.notifications.dto.response.NotificationResponseDTO;
import com.wealthpro.notifications.enums.NotificationCategory;
import com.wealthpro.notifications.enums.NotificationStatus;
import com.wealthpro.notifications.exception.GlobalExceptionHandler;
import com.wealthpro.notifications.exception.InvalidOperationException;
import com.wealthpro.notifications.exception.ResourceNotFoundException;
import com.wealthpro.notifications.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationRequestDTO requestDTO;
    private NotificationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

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
    // TEST 1: POST /api/notifications — 201 Created
    // ─────────────────────────────────────────
    @Test
    void testCreateNotification_Returns201() throws Exception {
        when(notificationService.createNotification(
                any(NotificationRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value(1))
                .andExpect(jsonPath("$.status").value("Unread"))
                .andExpect(jsonPath("$.category").value("Compliance"));

        verify(notificationService, times(1))
                .createNotification(any(NotificationRequestDTO.class));
    }

    // ─────────────────────────────────────────
    // TEST 2: POST — 400 Validation error
    // ─────────────────────────────────────────
    @Test
    void testCreateNotification_InvalidBody_Returns400() throws Exception {
        NotificationRequestDTO emptyRequest = new NotificationRequestDTO();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────
    // TEST 3: GET /api/notifications — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testGetAllNotifications_Returns200() throws Exception {
        when(notificationService.getAllNotifications())
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notificationId").value(1));

        verify(notificationService, times(1)).getAllNotifications();
    }

    // ─────────────────────────────────────────
    // TEST 4: GET /api/notifications/{id} — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testGetNotificationById_Returns200() throws Exception {
        when(notificationService.getNotificationById(1L))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(1))
                .andExpect(jsonPath("$.message")
                        .value("Your KYC has been verified"));

        verify(notificationService, times(1)).getNotificationById(1L);
    }

    // ─────────────────────────────────────────
    // TEST 5: GET /api/notifications/{id} — 404
    // ─────────────────────────────────────────
    @Test
    void testGetNotificationById_NotFound_Returns404() throws Exception {
        when(notificationService.getNotificationById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Notification not found with ID: 999"));

        mockMvc.perform(get("/api/notifications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Notification not found with ID: 999"));
    }

    // ─────────────────────────────────────────
    // TEST 6: GET /api/notifications/user/{userId} — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testGetNotificationsByUserId_Returns200() throws Exception {
        when(notificationService.getNotificationsByUserId(1L))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(1));

        verify(notificationService, times(1))
                .getNotificationsByUserId(1L);
    }

    // ─────────────────────────────────────────
    // TEST 7: GET /api/notifications/user/{userId}/unread — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testGetUnreadNotifications_Returns200() throws Exception {
        when(notificationService.getUnreadNotificationsByUserId(1L))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/notifications/user/1/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("Unread"));

        verify(notificationService, times(1))
                .getUnreadNotificationsByUserId(1L);
    }

    // ─────────────────────────────────────────
    // TEST 8: PUT /api/notifications/{id}/status — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testUpdateStatus_Returns200() throws Exception {
        NotificationStatusUpdateDTO statusRequest =
                new NotificationStatusUpdateDTO();
        statusRequest.setStatus(NotificationStatus.Read);

        responseDTO.setStatus(NotificationStatus.Read);

        when(notificationService.updateStatus(eq(1L),
                any(NotificationStatusUpdateDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/notifications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Read"));
    }

    // ─────────────────────────────────────────
    // TEST 9: PUT status — Dismissed → 400
    // ─────────────────────────────────────────
    @Test
    void testUpdateStatus_Dismissed_Returns400() throws Exception {
        NotificationStatusUpdateDTO statusRequest =
                new NotificationStatusUpdateDTO();
        statusRequest.setStatus(NotificationStatus.Read);

        when(notificationService.updateStatus(eq(1L),
                any(NotificationStatusUpdateDTO.class)))
                .thenThrow(new InvalidOperationException(
                        "Cannot update a Dismissed notification — it is a final state"));

        mockMvc.perform(put("/api/notifications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Cannot update a Dismissed notification — it is a final state"));
    }

    // ─────────────────────────────────────────
    // TEST 10: PUT /api/notifications/user/{userId}/read-all — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testMarkAllAsRead_Returns200() throws Exception {
        when(notificationService.markAllAsReadByUserId(1L)).thenReturn(2);

        mockMvc.perform(put("/api/notifications/user/1/read-all"))
                .andExpect(status().isOk())
                .andExpect(content().string("2 notifications marked as Read"));

        verify(notificationService, times(1)).markAllAsReadByUserId(1L);
    }

    // ─────────────────────────────────────────
    // TEST 11: DELETE /api/notifications/{id} — 200 OK
    // ─────────────────────────────────────────
    @Test
    void testDeleteNotification_Returns200() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification deleted successfully"));

        verify(notificationService, times(1)).deleteNotification(1L);
    }

    // ─────────────────────────────────────────
    // TEST 12: DELETE — 404 Not Found
    // ─────────────────────────────────────────
    @Test
    void testDeleteNotification_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Notification not found with ID: 999"))
                .when(notificationService).deleteNotification(999L);

        mockMvc.perform(delete("/api/notifications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Notification not found with ID: 999"));
    }
}
