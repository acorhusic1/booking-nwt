package com.bookingnwt.notificationservice.service;

import com.bookingnwt.notificationservice.dto.MessageRequestDTO;
import com.bookingnwt.notificationservice.dto.MessageResponseDTO;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.mapper.MessageMapper;
import com.bookingnwt.notificationservice.model.Conversation;
import com.bookingnwt.notificationservice.model.Message;
import com.bookingnwt.notificationservice.repository.ConversationRepository;
import com.bookingnwt.notificationservice.repository.MessageRepository;
import com.bookingnwt.notificationservice.repository.NotificationRepository;
import com.bookingnwt.notificationservice.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Conversation conversation;
    private Message message;
    private MessageRequestDTO requestDTO;
    private MessageResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        conversation = new Conversation();
        conversation.setId(1L);
        conversation.setGuestId(10L);
        conversation.setHostId(20L);

        message = new Message();
        message.setId(1L);
        message.setConversation(conversation);
        message.setSenderId(10L);
        message.setContent("Pozdrav!");
        message.setIsRead(false);
        message.setSentAt(LocalDateTime.now());

        requestDTO = new MessageRequestDTO();
        requestDTO.setConversationId(1L);
        requestDTO.setSenderId(10L);
        requestDTO.setContent("Pozdrav!");

        responseDTO = new MessageResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setConversationId(1L);
        responseDTO.setSenderId(10L);
        responseDTO.setContent("Pozdrav!");
        responseDTO.setIsRead(false);
        responseDTO.setSentAt(message.getSentAt());
    }

    @Test
    void sendMessage_Success() {
        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toDTO(message)).thenReturn(responseDTO);

        MessageResponseDTO result = messageService.sendMessage(requestDTO);

        assertNotNull(result);
        assertEquals("Pozdrav!", result.getContent());
        assertEquals(10L, result.getSenderId());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void sendMessage_ConversationNotFound() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());
        requestDTO.setConversationId(99L);

        assertThrows(ResourceNotFoundException.class, () -> messageService.sendMessage(requestDTO));
    }

    @Test
    void getMessageById_Success() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageMapper.toDTO(message)).thenReturn(responseDTO);

        MessageResponseDTO result = messageService.getMessageById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getMessageById_NotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.getMessageById(99L));
    }

    @Test
    void getMessagesByConversationId_Success() {
        when(messageRepository.findByConversationIdOrderBySentAtAsc(1L)).thenReturn(List.of(message));
        when(messageMapper.toDTO(message)).thenReturn(responseDTO);

        List<MessageResponseDTO> result = messageService.getMessagesByConversationId(1L);

        assertEquals(1, result.size());
        assertEquals("Pozdrav!", result.get(0).getContent());
    }

    @Test
    void markAsRead_Success() {
        MessageResponseDTO readResponse = new MessageResponseDTO();
        readResponse.setId(1L);
        readResponse.setIsRead(true);
        readResponse.setReadAt(LocalDateTime.now());

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toDTO(message)).thenReturn(readResponse);

        MessageResponseDTO result = messageService.markAsRead(1L);

        assertTrue(result.getIsRead());
        assertNotNull(result.getReadAt());
    }

    @Test
    void markAsRead_NotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> messageService.markAsRead(99L));
    }

    @Test
    void deleteMessage_Success() {
        when(messageRepository.existsById(1L)).thenReturn(true);
        doNothing().when(messageRepository).deleteById(1L);

        assertDoesNotThrow(() -> messageService.deleteMessage(1L));
        verify(messageRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteMessage_NotFound() {
        when(messageRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> messageService.deleteMessage(99L));
    }
}
