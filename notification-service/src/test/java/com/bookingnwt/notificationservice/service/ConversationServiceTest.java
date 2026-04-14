package com.bookingnwt.notificationservice.service;

import com.bookingnwt.notificationservice.dto.ConversationRequestDTO;
import com.bookingnwt.notificationservice.dto.ConversationResponseDTO;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.mapper.ConversationMapper;
import com.bookingnwt.notificationservice.model.Conversation;
import com.bookingnwt.notificationservice.repository.ConversationRepository;
import com.bookingnwt.notificationservice.service.impl.ConversationServiceImpl;
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
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private Conversation conversation;
    private ConversationRequestDTO requestDTO;
    private ConversationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        conversation = new Conversation();
        conversation.setId(1L);
        conversation.setGuestId(10L);
        conversation.setHostId(20L);
        conversation.setPropertyId(30L);
        conversation.setReservationId(40L);
        conversation.setCreatedAt(LocalDateTime.now());

        requestDTO = new ConversationRequestDTO();
        requestDTO.setGuestId(10L);
        requestDTO.setHostId(20L);
        requestDTO.setPropertyId(30L);
        requestDTO.setReservationId(40L);

        responseDTO = new ConversationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setGuestId(10L);
        responseDTO.setHostId(20L);
        responseDTO.setPropertyId(30L);
        responseDTO.setReservationId(40L);
        responseDTO.setCreatedAt(conversation.getCreatedAt());
    }

    @Test
    void createConversation_Success() {
        when(conversationMapper.toEntity(requestDTO)).thenReturn(conversation);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);
        when(conversationMapper.toDTO(conversation)).thenReturn(responseDTO);

        ConversationResponseDTO result = conversationService.createConversation(requestDTO);

        assertNotNull(result);
        assertEquals(10L, result.getGuestId());
        assertEquals(20L, result.getHostId());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void getConversationById_Success() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(conversationMapper.toDTO(conversation)).thenReturn(responseDTO);

        ConversationResponseDTO result = conversationService.getConversationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getConversationById_NotFound() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.getConversationById(99L));
    }

    @Test
    void getAllConversations_Success() {
        Conversation c2 = new Conversation();
        c2.setId(2L);
        ConversationResponseDTO r2 = new ConversationResponseDTO();
        r2.setId(2L);

        when(conversationRepository.findAll()).thenReturn(Arrays.asList(conversation, c2));
        when(conversationMapper.toDTO(conversation)).thenReturn(responseDTO);
        when(conversationMapper.toDTO(c2)).thenReturn(r2);

        List<ConversationResponseDTO> result = conversationService.getAllConversations();

        assertEquals(2, result.size());
    }

    @Test
    void getConversationsByGuestId_Success() {
        when(conversationRepository.findByGuestId(10L)).thenReturn(List.of(conversation));
        when(conversationMapper.toDTO(conversation)).thenReturn(responseDTO);

        List<ConversationResponseDTO> result = conversationService.getConversationsByGuestId(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getGuestId());
    }

    @Test
    void getConversationsByHostId_Success() {
        when(conversationRepository.findByHostId(20L)).thenReturn(List.of(conversation));
        when(conversationMapper.toDTO(conversation)).thenReturn(responseDTO);

        List<ConversationResponseDTO> result = conversationService.getConversationsByHostId(20L);

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getHostId());
    }

    @Test
    void deleteConversation_Success() {
        when(conversationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(conversationRepository).deleteById(1L);

        assertDoesNotThrow(() -> conversationService.deleteConversation(1L));
        verify(conversationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteConversation_NotFound() {
        when(conversationRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> conversationService.deleteConversation(99L));
    }
}
