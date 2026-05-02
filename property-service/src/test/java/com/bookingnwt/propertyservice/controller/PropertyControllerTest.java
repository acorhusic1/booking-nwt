package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.exception.GlobalExceptionHandler;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.service.PropertyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyController.class)
@Import(GlobalExceptionHandler.class)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PropertyService propertyService;

    private PropertyResponse createPropertyResponse() {
        PropertyResponse r = new PropertyResponse();
        r.setId(1L);
        r.setHostId(1L);
        r.setName("Apartman Centar");
        r.setDescription("Opis");
        r.setAddress("Ferhadija 1");
        r.setCity("Sarajevo");
        r.setCountry("BiH");
        r.setLatitude(new BigDecimal("43.856"));
        r.setLongitude(new BigDecimal("18.413"));
        r.setMaxGuests(4);
        r.setIsActive(true);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    @Test
    void getAllProperties_shouldReturn200() throws Exception {
        when(propertyService.getAllProperties()).thenReturn(List.of(createPropertyResponse()));

        mockMvc.perform(get("/api/properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apartman Centar"));
    }

    @Test
    void getPropertyById_shouldReturn200_whenExists() throws Exception {
        when(propertyService.getPropertyById(1L)).thenReturn(createPropertyResponse());

        mockMvc.perform(get("/api/properties/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Sarajevo"));
    }

    @Test
    void getPropertyById_shouldReturn404_whenNotFound() throws Exception {
        when(propertyService.getPropertyById(99L)).thenThrow(new ResourceNotFoundException("Nije pronađena"));

        mockMvc.perform(get("/api/properties/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPropertiesByHostId_shouldReturn200() throws Exception {
        when(propertyService.getPropertiesByHostId(1L)).thenReturn(List.of(createPropertyResponse()));

        mockMvc.perform(get("/api/properties/host/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostId").value(1));
    }

    @Test
    void getPropertiesByCity_shouldReturn200() throws Exception {
        when(propertyService.getPropertiesByCity("Sarajevo")).thenReturn(List.of(createPropertyResponse()));

        mockMvc.perform(get("/api/properties/city/Sarajevo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Sarajevo"));
    }

    @Test
    void createProperty_shouldReturn201_whenValid() throws Exception {
        PropertyRequest request = new PropertyRequest();
        request.setHostId(1L);
        request.setName("Apartman Centar");
        request.setAddress("Ferhadija 1");
        request.setCity("Sarajevo");
        request.setCountry("BiH");

        when(propertyService.createProperty(any())).thenReturn(createPropertyResponse());

        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apartman Centar"));
    }

    @Test
    void createProperty_shouldReturn400_whenInvalid() throws Exception {
        PropertyRequest request = new PropertyRequest();
        // Missing required fields

        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProperty_shouldReturn200() throws Exception {
        PropertyRequest request = new PropertyRequest();
        request.setHostId(1L);
        request.setName("Updated");
        request.setAddress("Nova adresa");
        request.setCity("Mostar");
        request.setCountry("BiH");

        when(propertyService.updateProperty(eq(1L), any())).thenReturn(createPropertyResponse());

        mockMvc.perform(put("/api/properties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProperty_shouldReturn204() throws Exception {
        doNothing().when(propertyService).deleteProperty(1L);

        mockMvc.perform(delete("/api/properties/1"))
                .andExpect(status().isNoContent());
    }
}
