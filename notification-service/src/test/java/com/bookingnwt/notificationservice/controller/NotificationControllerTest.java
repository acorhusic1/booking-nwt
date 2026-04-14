package com.bookingnwt.notificationservice.controller;

import com.bookingnwt.notificationservice.dto.NotificationRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationResponseDTO;
import com.bookingnwt.notificationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private ObjectMapper objectMapper;
    private NotificationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        responseDTO = new NotificationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUserId(10L);
        responseDTO.setType("BOOKING");
        responseDTO.setTitle("Nova rezervacija");
        responseDTO.setContent("Imate novu rezervaciju");
        responseDTO.setIsRead(false);
        responseDTO.setRelatedReservationId(100L);
        responseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createNotification_Success() throws Exception {
        when(notificationService.createNotification(any(NotificationRequestDTO.class))).thenReturn(responseDTO);

        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setUserId(10L);
        request.setType("BOOKING");
        request.setTitle("Nova rezervacija");
        request.setContent("Imate novu rezervaciju");
        request.setRelatedReservationId(100L);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("BOOKING"))
                .andExpect(jsonPath("$.title").value("Nova rezervacija"));
    }

    @Test
    void createNotification_ValidationError() throws Exception {
        NotificationRequestDTO request = new NotificationRequestDTO();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNotificationById_Success() throws Exception {
        when(notificationService.getNotificationById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("BOOKING"));
    }

    @Test
    void getNotificationById_NotFound() throws Exception {
        when(notificationService.getNotificationById(99L)).thenThrow(new ResourceNotFoundException("Notifikacija sa ID 99 nije pronađena"));

        mockMvc.perform(get("/api/notifications/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllNotifications_Success() throws Exception {
        NotificationResponseDTO r2 = new NotificationResponseDTO();
        r2.setId(2L);
        r2.setType("MESSAGE");

        when(notificationService.getAllNotifications()).thenReturn(Arrays.asList(responseDTO, r2));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getNotificationsByUserId_Success() throws Exception {
        when(notificationService.getNotificationsByUserId(10L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/notifications/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(10));
    }

    @Test
    void markAsRead_Success() throws Exception {
        NotificationResponseDTO readResponse = new NotificationResponseDTO();
        readResponse.setId(1L);
        readResponse.setIsRead(true);
        readResponse.setReadAt(LocalDateTime.now());

        when(notificationService.markAsRead(1L)).thenReturn(readResponse);

        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    void deleteNotification_Success() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteNotification_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Notifikacija sa ID 99 nije pronađena"))
                .when(notificationService).deleteNotification(99L);

        mockMvc.perform(delete("/api/notifications/99"))
                .andExpect(status().isNotFound());
    }
}
