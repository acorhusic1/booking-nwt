package com.bookingnwt.notificationservice.controller;

import com.bookingnwt.notificationservice.dto.MessageRequestDTO;
import com.bookingnwt.notificationservice.dto.MessageResponseDTO;
import com.bookingnwt.notificationservice.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(@Valid @RequestBody MessageRequestDTO dto) {
        return new ResponseEntity<>(messageService.sendMessage(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessagesByConversationId(@PathVariable Long conversationId) {
        return ResponseEntity.ok(messageService.getMessagesByConversationId(conversationId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponseDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
