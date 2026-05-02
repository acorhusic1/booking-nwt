package com.bookingnwt.notificationservice.service.impl;

import com.bookingnwt.notificationservice.dto.MessageRequestDTO;
import com.bookingnwt.notificationservice.dto.MessageResponseDTO;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.mapper.MessageMapper;
import com.bookingnwt.notificationservice.model.Conversation;
import com.bookingnwt.notificationservice.model.Message;
import com.bookingnwt.notificationservice.repository.ConversationRepository;
import com.bookingnwt.notificationservice.repository.MessageRepository;
import com.bookingnwt.notificationservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;

    @Override
    public MessageResponseDTO sendMessage(MessageRequestDTO dto) {
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Konverzacija sa ID " + dto.getConversationId() + " nije pronađena"));

        Message message = new Message(conversation, dto.getSenderId(), dto.getContent());
        return messageMapper.toDTO(messageRepository.save(message));
    }

    @Override
    public MessageResponseDTO getMessageById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poruka sa ID " + id + " nije pronađena"));
        return messageMapper.toDTO(message);
    }

    @Override
    public List<MessageResponseDTO> getMessagesByConversationId(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId).stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponseDTO markAsRead(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poruka sa ID " + id + " nije pronađena"));
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        return messageMapper.toDTO(messageRepository.save(message));
    }

    @Override
    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Poruka sa ID " + id + " nije pronađena");
        }
        messageRepository.deleteById(id);
    }
}
