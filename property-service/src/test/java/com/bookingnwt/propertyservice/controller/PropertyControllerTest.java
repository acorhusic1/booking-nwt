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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyController.class)
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PropertyService propertyService;

    // Feign klijent se ne kreira u @WebMvcTest slice-u — bez mocka kontekst pada
    @MockitoBean
    private com.bookingnwt.propertyservice.client.UserClient userClient;

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
        when(propertyService.getAllProperties(any())).thenReturn(new PageImpl<>(List.of(createPropertyResponse())));

        mockMvc.perform(get("/api/properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Apartman Centar"));
    }

    @Test
    void searchAvailableProperties_shouldReturn200() throws Exception {
        when(propertyService.getAvailableProperties(eq("Sarajevo"), any(), any()))
                .thenReturn(List.of(createPropertyResponse()));

        mockMvc.perform(get("/api/properties/search")
                        .param("city", "Sarajevo")
                        .param("startDate", "2024-06-01")
                        .param("endDate", "2024-06-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Sarajevo"));
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

        // F16 — host mora imati APPROVED verifikaciju da bi objavio objekat
        when(userClient.getVerifications(1L))
                .thenReturn(List.of(java.util.Map.of("status", "APPROVED")));
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
    @Test
    void patchProperty_shouldReturn200() throws Exception {
        com.bookingnwt.propertyservice.dto.PropertyPatchRequest request = new com.bookingnwt.propertyservice.dto.PropertyPatchRequest();
        request.setName("Patched Name");
        request.setAvailable(false);

        PropertyResponse response = createPropertyResponse();
        response.setName("Patched Name");
        response.setAvailable(false);

        when(propertyService.patchProperty(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/properties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Patched Name"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void batchCreateProperties_shouldReturn201() throws Exception {
        PropertyRequest request1 = new PropertyRequest();
        request1.setHostId(1L);
        request1.setName("Prop 1");
        request1.setCity("Sarajevo");
        request1.setCountry("BiH");
        request1.setAddress("A1");

        PropertyRequest request2 = new PropertyRequest();
        request2.setHostId(1L);
        request2.setName("Prop 2");
        request2.setCity("Mostar");
        request2.setCountry("BiH");
        request2.setAddress("A2");

        List<PropertyRequest> requests = List.of(request1, request2);
        List<PropertyResponse> responses = List.of(createPropertyResponse(), createPropertyResponse());
        responses.get(1).setName("Prop 2");
        responses.get(1).setCity("Mostar");

        when(propertyService.batchCreateProperties(any())).thenReturn(responses);

        mockMvc.perform(post("/api/properties/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].name").value("Prop 2"));
    }
}
