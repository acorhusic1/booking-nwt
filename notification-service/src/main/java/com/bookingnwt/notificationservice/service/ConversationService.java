package com.bookingnwt.notificationservice.service;

import com.bookingnwt.notificationservice.dto.ConversationRequestDTO;
import com.bookingnwt.notificationservice.dto.ConversationResponseDTO;

import java.util.List;

public interface ConversationService {
    ConversationResponseDTO createConversation(ConversationRequestDTO dto);
    ConversationResponseDTO getConversationById(Long id);
    List<ConversationResponseDTO> getAllConversations();
    List<ConversationResponseDTO> getConversationsByGuestId(Long guestId);
    List<ConversationResponseDTO> getConversationsByHostId(Long hostId);
    void deleteConversation(Long id);
}
