package com.bookingnwt.notificationservice.service.impl;

import com.bookingnwt.notificationservice.dto.MessageRequestDTO;
import com.bookingnwt.notificationservice.dto.MessageResponseDTO;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.mapper.MessageMapper;
import com.bookingnwt.notificationservice.model.Conversation;
import com.bookingnwt.notificationservice.model.Message;
import com.bookingnwt.notificationservice.model.Notification;
import com.bookingnwt.notificationservice.repository.ConversationRepository;
import com.bookingnwt.notificationservice.repository.MessageRepository;
import com.bookingnwt.notificationservice.repository.NotificationRepository;
import com.bookingnwt.notificationservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;
    private final NotificationRepository notificationRepository;

    @Override
    public MessageResponseDTO sendMessage(MessageRequestDTO dto) {
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Konverzacija sa ID " + dto.getConversationId() + " nije pronađena"));

        Message message = new Message(conversation, dto.getSenderId(), dto.getContent());
        MessageResponseDTO saved = messageMapper.toDTO(messageRepository.save(message));

        // F8/F9 — "Korisnik dobija notifikaciju o novoj poruci": primalac je
        // druga strana konverzacije (gost ili host).
        try {
            Long recipientId = dto.getSenderId() != null && dto.getSenderId().equals(conversation.getGuestId())
                    ? conversation.getHostId()
                    : conversation.getGuestId();
            if (recipientId != null) {
                String excerpt = dto.getContent() != null && dto.getContent().length() > 80
                        ? dto.getContent().substring(0, 80) + "…"
                        : dto.getContent();
                notificationRepository.save(new Notification(
                        recipientId,
                        "NOVA_PORUKA",
                        "Nova poruka",
                        String.format("Korisnik #%d vam je poslao poruku: %s",
                                dto.getSenderId(), excerpt != null ? excerpt : ""),
                        conversation.getReservationId()
                ));
            }
        } catch (Exception e) {
            log.warn("⚠️ Notifikacija o novoj poruci nije kreirana: {}", e.getMessage());
        }

        return saved;
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
