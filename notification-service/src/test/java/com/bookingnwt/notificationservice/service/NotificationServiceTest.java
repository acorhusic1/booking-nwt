package com.bookingnwt.notificationservice.service;

import com.bookingnwt.notificationservice.dto.NotificationRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationResponseDTO;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.mapper.NotificationMapper;
import com.bookingnwt.notificationservice.model.Notification;
import com.bookingnwt.notificationservice.repository.NotificationRepository;
import com.bookingnwt.notificationservice.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;
    private NotificationRequestDTO requestDTO;
    private NotificationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setUserId(10L);
        notification.setType("BOOKING");
        notification.setTitle("Nova rezervacija");
        notification.setContent("Imate novu rezervaciju");
        notification.setIsRead(false);
        notification.setRelatedReservationId(100L);
        notification.setCreatedAt(LocalDateTime.now());

        requestDTO = new NotificationRequestDTO();
        requestDTO.setUserId(10L);
        requestDTO.setType("BOOKING");
        requestDTO.setTitle("Nova rezervacija");
        requestDTO.setContent("Imate novu rezervaciju");
        requestDTO.setRelatedReservationId(100L);

        responseDTO = new NotificationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUserId(10L);
        responseDTO.setType("BOOKING");
        responseDTO.setTitle("Nova rezervacija");
        responseDTO.setContent("Imate novu rezervaciju");
        responseDTO.setIsRead(false);
        responseDTO.setRelatedReservationId(100L);
        responseDTO.setCreatedAt(notification.getCreatedAt());
    }

    @Test
    void createNotification_Success() {
        when(notificationMapper.toEntity(requestDTO)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDTO(notification)).thenReturn(responseDTO);

        NotificationResponseDTO result = notificationService.createNotification(requestDTO);

        assertNotNull(result);
        assertEquals("BOOKING", result.getType());
        assertEquals("Nova rezervacija", result.getTitle());
        assertFalse(result.getIsRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void getNotificationById_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationMapper.toDTO(notification)).thenReturn(responseDTO);

        NotificationResponseDTO result = notificationService.getNotificationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getNotificationById_NotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.getNotificationById(99L));
    }

    @Test
    void getAllNotifications_Success() {
        Notification n2 = new Notification();
        n2.setId(2L);
        NotificationResponseDTO r2 = new NotificationResponseDTO();
        r2.setId(2L);

        when(notificationRepository.findAll()).thenReturn(Arrays.asList(notification, n2));
        when(notificationMapper.toDTO(notification)).thenReturn(responseDTO);
        when(notificationMapper.toDTO(n2)).thenReturn(r2);

        List<NotificationResponseDTO> result = notificationService.getAllNotifications();

        assertEquals(2, result.size());
    }

    @Test
    void getNotificationsByUserId_Success() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(notification));
        when(notificationMapper.toDTO(notification)).thenReturn(responseDTO);

        List<NotificationResponseDTO> result = notificationService.getNotificationsByUserId(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getUserId());
    }

    @Test
    void markAsRead_Success() {
        NotificationResponseDTO readResponse = new NotificationResponseDTO();
        readResponse.setId(1L);
        readResponse.setIsRead(true);
        readResponse.setReadAt(LocalDateTime.now());

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toDTO(notification)).thenReturn(readResponse);

        NotificationResponseDTO result = notificationService.markAsRead(1L);

        assertNotNull(result);
        assertTrue(result.getIsRead());
        assertNotNull(result.getReadAt());
    }

    @Test
    void markAsRead_NotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(99L));
    }

    @Test
    void deleteNotification_Success() {
        when(notificationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(1L);

        assertDoesNotThrow(() -> notificationService.deleteNotification(1L));
        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteNotification_NotFound() {
        when(notificationRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> notificationService.deleteNotification(99L));
    }
}
