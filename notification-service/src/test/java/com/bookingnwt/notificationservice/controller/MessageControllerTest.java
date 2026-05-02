package com.bookingnwt.notificationservice.controller;

import com.bookingnwt.notificationservice.dto.MessageRequestDTO;
import com.bookingnwt.notificationservice.dto.MessageResponseDTO;
import com.bookingnwt.notificationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@Import(GlobalExceptionHandler.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    private ObjectMapper objectMapper;
    private MessageResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        responseDTO = new MessageResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setConversationId(1L);
        responseDTO.setSenderId(10L);
        responseDTO.setContent("Pozdrav!");
        responseDTO.setIsRead(false);
        responseDTO.setSentAt(LocalDateTime.now());
    }

    @Test
    void sendMessage_Success() throws Exception {
        when(messageService.sendMessage(any(MessageRequestDTO.class))).thenReturn(responseDTO);

        MessageRequestDTO request = new MessageRequestDTO();
        request.setConversationId(1L);
        request.setSenderId(10L);
        request.setContent("Pozdrav!");

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Pozdrav!"))
                .andExpect(jsonPath("$.senderId").value(10));
    }

    @Test
    void sendMessage_ValidationError() throws Exception {
        MessageRequestDTO request = new MessageRequestDTO();

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMessageById_Success() throws Exception {
        when(messageService.getMessageById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Pozdrav!"));
    }

    @Test
    void getMessageById_NotFound() throws Exception {
        when(messageService.getMessageById(99L)).thenThrow(new ResourceNotFoundException("Poruka sa ID 99 nije pronađena"));

        mockMvc.perform(get("/api/messages/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMessagesByConversationId_Success() throws Exception {
        when(messageService.getMessagesByConversationId(1L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/messages/conversation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void markAsRead_Success() throws Exception {
        MessageResponseDTO readResponse = new MessageResponseDTO();
        readResponse.setId(1L);
        readResponse.setIsRead(true);
        readResponse.setReadAt(LocalDateTime.now());

        when(messageService.markAsRead(1L)).thenReturn(readResponse);

        mockMvc.perform(put("/api/messages/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
    }

    @Test
    void deleteMessage_Success() throws Exception {
        doNothing().when(messageService).deleteMessage(1L);

        mockMvc.perform(delete("/api/messages/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMessage_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Poruka sa ID 99 nije pronađena"))
                .when(messageService).deleteMessage(99L);

        mockMvc.perform(delete("/api/messages/99"))
                .andExpect(status().isNotFound());
    }
}
