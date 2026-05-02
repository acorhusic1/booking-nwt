package com.bookingnwt.notificationservice.controller;

import com.bookingnwt.notificationservice.dto.ConversationRequestDTO;
import com.bookingnwt.notificationservice.dto.ConversationResponseDTO;
import com.bookingnwt.notificationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.notificationservice.exception.ResourceNotFoundException;
import com.bookingnwt.notificationservice.service.ConversationService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversationController.class)
@Import(GlobalExceptionHandler.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConversationService conversationService;

    private ObjectMapper objectMapper;
    private ConversationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().findAndAddModules().build();

        responseDTO = new ConversationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setGuestId(10L);
        responseDTO.setHostId(20L);
        responseDTO.setPropertyId(30L);
        responseDTO.setReservationId(40L);
        responseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createConversation_Success() throws Exception {
        when(conversationService.createConversation(any(ConversationRequestDTO.class))).thenReturn(responseDTO);

        ConversationRequestDTO request = new ConversationRequestDTO();
        request.setGuestId(10L);
        request.setHostId(20L);
        request.setPropertyId(30L);
        request.setReservationId(40L);

        mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestId").value(10))
                .andExpect(jsonPath("$.hostId").value(20));
    }

    @Test
    void createConversation_ValidationError() throws Exception {
        ConversationRequestDTO request = new ConversationRequestDTO();

        mockMvc.perform(post("/api/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getConversationById_Success() throws Exception {
        when(conversationService.getConversationById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/conversations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getConversationById_NotFound() throws Exception {
        when(conversationService.getConversationById(99L)).thenThrow(new ResourceNotFoundException("Konverzacija sa ID 99 nije pronađena"));

        mockMvc.perform(get("/api/conversations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllConversations_Success() throws Exception {
        ConversationResponseDTO r2 = new ConversationResponseDTO();
        r2.setId(2L);

        when(conversationService.getAllConversations()).thenReturn(Arrays.asList(responseDTO, r2));

        mockMvc.perform(get("/api/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getConversationsByGuestId_Success() throws Exception {
        when(conversationService.getConversationsByGuestId(10L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/conversations/guest/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].guestId").value(10));
    }

    @Test
    void getConversationsByHostId_Success() throws Exception {
        when(conversationService.getConversationsByHostId(20L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/conversations/host/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].hostId").value(20));
    }

    @Test
    void deleteConversation_Success() throws Exception {
        doNothing().when(conversationService).deleteConversation(1L);

        mockMvc.perform(delete("/api/conversations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteConversation_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Konverzacija sa ID 99 nije pronađena"))
                .when(conversationService).deleteConversation(99L);

        mockMvc.perform(delete("/api/conversations/99"))
                .andExpect(status().isNotFound());
    }
}
