package com.bookingnwt.notificationservice.service;

import com.bookingnwt.notificationservice.dto.MessageRequestDTO;
import com.bookingnwt.notificationservice.dto.MessageResponseDTO;

import java.util.List;

public interface MessageService {
    MessageResponseDTO sendMessage(MessageRequestDTO dto);
    MessageResponseDTO getMessageById(Long id);
    List<MessageResponseDTO> getMessagesByConversationId(Long conversationId);
    MessageResponseDTO markAsRead(Long id);
    void deleteMessage(Long id);
}
