package com.bookingnwt.userservice.controller;

import com.bookingnwt.userservice.dto.UserPreferenceRequest;
import com.bookingnwt.userservice.dto.UserPreferenceResponse;
import com.bookingnwt.userservice.exception.GlobalExceptionHandler;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.service.UserPreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserPreferenceController.class)
@Import(GlobalExceptionHandler.class)
class UserPreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserPreferenceService preferenceService;

    private UserPreferenceResponse createResponse() {
        UserPreferenceResponse r = new UserPreferenceResponse();
        r.setId(1L);
        r.setUserId(1L);
        r.setPreferredLanguage("bs");
        r.setPropertyType("APARTMENT");
        r.setMinPrice(new BigDecimal("50"));
        r.setMaxPrice(new BigDecimal("200"));
        r.setUpdatedAt(LocalDateTime.now());
        return r;
    }

    @Test
    void getPreference_shouldReturn200() throws Exception {
        when(preferenceService.getPreferenceByUserId(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/users/1/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredLanguage").value("bs"));
    }

    @Test
    void getPreference_shouldReturn404_whenNotFound() throws Exception {
        when(preferenceService.getPreferenceByUserId(99L))
                .thenThrow(new ResourceNotFoundException("Preferencije nisu pronađene"));

        mockMvc.perform(get("/api/users/99/preferences"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrUpdatePreference_shouldReturn200() throws Exception {
        UserPreferenceRequest request = new UserPreferenceRequest(1L, "bs", "APARTMENT",
                new BigDecimal("50"), new BigDecimal("200"));
        when(preferenceService.createOrUpdatePreference(any(UserPreferenceRequest.class)))
                .thenReturn(createResponse());

        mockMvc.perform(put("/api/users/1/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyType").value("APARTMENT"));
    }

    @Test
    void deletePreference_shouldReturn204() throws Exception {
        doNothing().when(preferenceService).deletePreference(1L);

        mockMvc.perform(delete("/api/users/1/preferences"))
                .andExpect(status().isNoContent());
    }
}
