package com.bookingnwt.userservice.controller;

import com.bookingnwt.userservice.dto.IdentityVerificationRequest;
import com.bookingnwt.userservice.dto.IdentityVerificationResponse;
import com.bookingnwt.userservice.exception.GlobalExceptionHandler;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.service.IdentityVerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IdentityVerificationController.class)
@Import(GlobalExceptionHandler.class)
class IdentityVerificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IdentityVerificationService verificationService;

    private IdentityVerificationResponse createResponse() {
        IdentityVerificationResponse r = new IdentityVerificationResponse();
        r.setId(1L);
        r.setUserId(1L);
        r.setDocumentType("LIČNA KARTA");
        r.setDocumentNumber("123456789");
        r.setStatus("PENDING");
        r.setSubmittedAt(LocalDateTime.now());
        return r;
    }

    @Test
    void getVerifications_shouldReturn200() throws Exception {
        when(verificationService.getVerificationsByUserId(1L))
                .thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/users/1/verifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentType").value("LIČNA KARTA"));
    }

    @Test
    void getVerifications_shouldReturn404_whenUserNotFound() throws Exception {
        when(verificationService.getVerificationsByUserId(99L))
                .thenThrow(new ResourceNotFoundException("Korisnik nije pronađen"));

        mockMvc.perform(get("/api/users/99/verifications"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getVerificationById_shouldReturn200() throws Exception {
        when(verificationService.getVerificationById(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/users/1/verifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createVerification_shouldReturn201() throws Exception {
        IdentityVerificationRequest request = new IdentityVerificationRequest(1L, "LIČNA KARTA", "123456789");
        when(verificationService.createVerification(any(IdentityVerificationRequest.class)))
                .thenReturn(createResponse());

        mockMvc.perform(post("/api/users/1/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentNumber").value("123456789"));
    }

    @Test
    void createVerification_shouldReturn400_whenInvalid() throws Exception {
        IdentityVerificationRequest request = new IdentityVerificationRequest(null, "", "");

        mockMvc.perform(post("/api/users/1/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
