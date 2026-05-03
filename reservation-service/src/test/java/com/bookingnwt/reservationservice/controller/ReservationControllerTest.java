package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.security.JwtAuthenticationFilter;
import com.bookingnwt.reservationservice.security.JwtTokenProvider;
import com.bookingnwt.reservationservice.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private ReservationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new ReservationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setGuestId(10L);
        responseDTO.setHostId(20L);
        responseDTO.setPropertyId(30L);
        responseDTO.setCheckIn(LocalDate.of(2025, 8, 1));
        responseDTO.setCheckOut(LocalDate.of(2025, 8, 5));
        responseDTO.setNumGuests(2);
        responseDTO.setTotalPrice(new BigDecimal("500.00"));
        responseDTO.setStatus(ReservationStatus.CREATED);
        responseDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createReservation_Returns201() throws Exception {
        when(reservationService.createReservation(any(ReservationRequestDTO.class))).thenReturn(responseDTO);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setGuestId(10L);
        request.setHostId(20L);
        request.setPropertyId(30L);
        request.setCheckIn(LocalDate.now().plusDays(30));
        request.setCheckOut(LocalDate.now().plusDays(35));
        request.setNumGuests(2);
        request.setTotalPrice(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.guestId").value(10));
    }

    @Test
    void getReservation_Returns200() throws Exception {
        when(reservationService.getReservationById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getReservation_NotFound() throws Exception {
        when(reservationService.getReservationById(99L)).thenThrow(new ResourceNotFoundException("Rezervacija nije pronađena sa ID: 99"));

        mockMvc.perform(get("/api/reservations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllReservations_Returns200() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByGuest_Returns200() throws Exception {
        when(reservationService.getReservationsByGuest(10L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reservations/guest/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByProperty_Returns200() throws Exception {
        when(reservationService.getReservationsByProperty(30L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reservations/property/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByHost_Returns200() throws Exception {
        when(reservationService.getReservationsByHost(20L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reservations/host/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateStatus_Returns200() throws Exception {
        responseDTO.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.updateStatus(eq(1L), eq(ReservationStatus.CONFIRMED))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/reservations/1/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelReservation_Returns200() throws Exception {
        responseDTO.setStatus(ReservationStatus.CANCELLED);
        when(reservationService.cancelReservation(1L)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void deleteReservation_Returns204() throws Exception {
        doNothing().when(reservationService).deleteReservation(1L);

        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isNoContent());
    }
}
