package com.bookingnwt.notificationservice.controller;

import com.bookingnwt.notificationservice.dto.NotificationBatchRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationPatchDTO;
import com.bookingnwt.notificationservice.dto.NotificationRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationResponseDTO;
import com.bookingnwt.notificationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
                com.bookingnwt.notificationservice.security.SecurityConfig.class,
                com.bookingnwt.notificationservice.security.JwtAuthenticationFilter.class,
                com.bookingnwt.notificationservice.security.FeignClientInterceptor.class
        }))
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
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
        when(notificationService.getNotificationById(99L))
                .thenThrow(new ResourceNotFoundException("Notifikacija sa ID 99 nije pronađena"));

        mockMvc.perform(get("/api/notifications/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllNotifications_Pageable_Success() throws Exception {
        NotificationResponseDTO r2 = new NotificationResponseDTO();
        r2.setId(2L);
        r2.setType("MESSAGE");

        Page<NotificationResponseDTO> page = new PageImpl<>(Arrays.asList(responseDTO, r2), PageRequest.of(0, 20), 2);
        when(notificationService.getAllNotifications(any())).thenReturn(page);

        mockMvc.perform(get("/api/notifications?page=0&size=20&sort=createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void search_WithFilters_Success() throws Exception {
        Page<NotificationResponseDTO> page = new PageImpl<>(List.of(responseDTO), PageRequest.of(0, 10), 1);
        when(notificationService.search(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/notifications/search?userId=10&type=BOOKING&isRead=false&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(10));
    }

    @Test
    void countUnreadByUserId_Success() throws Exception {
        when(notificationService.countUnreadByUserId(10L)).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/user/10/unread/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void createBatch_Success() throws Exception {
        NotificationRequestDTO n1 = new NotificationRequestDTO();
        n1.setUserId(1L); n1.setType("X"); n1.setTitle("T1");
        NotificationRequestDTO n2 = new NotificationRequestDTO();
        n2.setUserId(2L); n2.setType("X"); n2.setTitle("T2");

        NotificationBatchRequestDTO batch = new NotificationBatchRequestDTO();
        batch.setNotifications(Arrays.asList(n1, n2));

        when(notificationService.createBatch(any())).thenReturn(List.of(responseDTO, responseDTO));

        mockMvc.perform(post("/api/notifications/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createBatch_EmptyList_ValidationError() throws Exception {
        NotificationBatchRequestDTO batch = new NotificationBatchRequestDTO();
        batch.setNotifications(List.of());

        mockMvc.perform(post("/api/notifications/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchNotification_Success() throws Exception {
        NotificationPatchDTO patch = new NotificationPatchDTO();
        patch.setIsRead(true);

        NotificationResponseDTO patched = new NotificationResponseDTO();
        patched.setId(1L);
        patched.setIsRead(true);
        patched.setReadAt(LocalDateTime.now());

        when(notificationService.patchNotification(any(Long.class), any(NotificationPatchDTO.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/notifications/1")
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    void patchNotification_NotFound() throws Exception {
        NotificationPatchDTO patch = new NotificationPatchDTO();
        patch.setTitle("Novi naslov");

        when(notificationService.patchNotification(anyLong(), any(NotificationPatchDTO.class)))
                .thenThrow(new ResourceNotFoundException("Notifikacija sa ID 99 nije pronađena"));

        mockMvc.perform(patch("/api/notifications/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
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
