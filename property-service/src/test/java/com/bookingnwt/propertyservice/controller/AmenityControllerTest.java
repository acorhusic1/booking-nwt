package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.AmenityRequest;
import com.bookingnwt.propertyservice.dto.AmenityResponse;
import com.bookingnwt.propertyservice.exception.GlobalExceptionHandler;
import com.bookingnwt.propertyservice.service.AmenityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AmenityController.class)
@Import(GlobalExceptionHandler.class)
class AmenityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AmenityService amenityService;

    private AmenityResponse createAmenityResponse() {
        AmenityResponse r = new AmenityResponse();
        r.setId(1L);
        r.setName("WiFi");
        r.setCategory("BASIC");
        return r;
    }

    @Test
    void getAllAmenities_shouldReturn200() throws Exception {
        when(amenityService.getAllAmenities()).thenReturn(List.of(createAmenityResponse()));

        mockMvc.perform(get("/api/amenities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("WiFi"));
    }

    @Test
    void getAmenityById_shouldReturn200() throws Exception {
        when(amenityService.getAmenityById(1L)).thenReturn(createAmenityResponse());

        mockMvc.perform(get("/api/amenities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("BASIC"));
    }

    @Test
    void createAmenity_shouldReturn201_whenValid() throws Exception {
        AmenityRequest request = new AmenityRequest();
        request.setName("Pool");
        request.setCategory("LUXURY");

        when(amenityService.createAmenity(any())).thenReturn(createAmenityResponse());

        mockMvc.perform(post("/api/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createAmenity_shouldReturn400_whenInvalid() throws Exception {
        AmenityRequest request = new AmenityRequest();
        // Missing required fields

        mockMvc.perform(post("/api/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
