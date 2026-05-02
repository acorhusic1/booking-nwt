package com.bookingnwt.notificationservice.service.impl;

import com.bookingnwt.notificationservice.dto.ConversationRequestDTO;
import com.bookingnwt.notificationservice.dto.ConversationResponseDTO;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.mapper.ConversationMapper;
import com.bookingnwt.notificationservice.model.Conversation;
import com.bookingnwt.notificationservice.repository.ConversationRepository;
import com.bookingnwt.notificationservice.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;

    @Override
    public ConversationResponseDTO createConversation(ConversationRequestDTO dto) {
        Conversation conversation = conversationMapper.toEntity(dto);
        return conversationMapper.toDTO(conversationRepository.save(conversation));
    }

    @Override
    public ConversationResponseDTO getConversationById(Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Konverzacija sa ID " + id + " nije pronađena"));
        return conversationMapper.toDTO(conversation);
    }

    @Override
    public List<ConversationResponseDTO> getAllConversations() {
        return conversationRepository.findAll().stream()
                .map(conversationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationResponseDTO> getConversationsByGuestId(Long guestId) {
        return conversationRepository.findByGuestId(guestId).stream()
                .map(conversationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationResponseDTO> getConversationsByHostId(Long hostId) {
        return conversationRepository.findByHostId(hostId).stream()
                .map(conversationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteConversation(Long id) {
        if (!conversationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Konverzacija sa ID " + id + " nije pronađena");
        }
        conversationRepository.deleteById(id);
    }
}
