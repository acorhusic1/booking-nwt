package com.bookingnwt.notificationservice.controller;

import com.bookingnwt.notificationservice.dto.ConversationRequestDTO;
import com.bookingnwt.notificationservice.dto.ConversationResponseDTO;
import com.bookingnwt.notificationservice.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponseDTO> createConversation(@Valid @RequestBody ConversationRequestDTO dto) {
        return new ResponseEntity<>(conversationService.createConversation(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationResponseDTO> getConversationById(@PathVariable Long id) {
        return ResponseEntity.ok(conversationService.getConversationById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ConversationResponseDTO>> getAllConversations() {
        return ResponseEntity.ok(conversationService.getAllConversations());
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationResponseDTO>> getConversationsByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(conversationService.getConversationsByGuestId(guestId));
    }

    @GetMapping("/host/{hostId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationResponseDTO>> getConversationsByHostId(@PathVariable Long hostId) {
        return ResponseEntity.ok(conversationService.getConversationsByHostId(hostId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}
