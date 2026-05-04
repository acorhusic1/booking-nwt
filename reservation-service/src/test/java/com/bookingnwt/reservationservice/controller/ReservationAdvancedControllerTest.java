package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.exception.GlobalExceptionHandler;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.security.JwtAuthenticationFilter;
import com.bookingnwt.reservationservice.security.JwtTokenProvider;
import com.bookingnwt.reservationservice.service.ReservationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ReservationAdvancedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private ReservationResponseDTO sample;

    @BeforeEach
    void setUp() {
        sample = new ReservationResponseDTO();
        sample.setId(1L);
        sample.setGuestId(10L);
        sample.setHostId(20L);
        sample.setPropertyId(30L);
        sample.setCheckIn(LocalDate.of(2026, 6, 1));
        sample.setCheckOut(LocalDate.of(2026, 6, 5));
        sample.setNumGuests(2);
        sample.setTotalPrice(new BigDecimal("400.00"));
        sample.setStatus(ReservationStatus.CREATED);
    }

    @Test
    void patchReservation_Returns200() throws Exception {
        when(reservationService.patchReservation(eq(1L), any(JsonNode.class))).thenReturn(sample);

        String patch = "[{\"op\":\"replace\",\"path\":\"/numGuests\",\"value\":3}]";

        mockMvc.perform(patch("/api/reservations/1")
                        .contentType("application/json-patch+json")
                        .content(patch))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patchReservation_InvalidPatch_Returns400() throws Exception {
        when(reservationService.patchReservation(eq(1L), any(JsonNode.class)))
                .thenThrow(new IllegalArgumentException("Neispravna JSON Patch operacija"));

        String patch = "[{\"op\":\"bogus\",\"path\":\"/x\"}]";
        mockMvc.perform(patch("/api/reservations/1")
                        .contentType("application/json-patch+json")
                        .content(patch))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByGuestPaged_Returns200() throws Exception {
        Page<ReservationResponseDTO> page = new PageImpl<>(List.of(sample));
        when(reservationService.getReservationsByGuestPaged(eq(10L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/reservations/guest/10/paged?page=0&size=10&sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getByGuestAndDateRange_Returns200() throws Exception {
        when(reservationService.getReservationsByGuestAndDateRange(
                eq(10L),
                eq(LocalDate.parse("2026-01-01")),
                eq(LocalDate.parse("2026-12-31"))))
                .thenReturn(List.of(sample));

        mockMvc.perform(get("/api/reservations/guest/10/range")
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getByGuestAndDateRange_BadRange_Returns400() throws Exception {
        when(reservationService.getReservationsByGuestAndDateRange(
                eq(10L), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new IllegalArgumentException("Neispravan opseg"));

        mockMvc.perform(get("/api/reservations/guest/10/range")
                        .param("from", "2026-12-31")
                        .param("to", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void hostRevenue_Returns200() throws Exception {
        when(reservationService.getHostRevenue(20L)).thenReturn(new BigDecimal("1234.56"));

        mockMvc.perform(get("/api/reservations/host/20/revenue"))
                .andExpect(status().isOk())
                .andExpect(content().string("1234.56"));
    }

    @Test
    void batchCreate_Returns201() throws Exception {
        when(reservationService.batchCreate(anyList())).thenReturn(List.of(sample, sample));

        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setGuestId(10L);
        req.setHostId(20L);
        req.setPropertyId(30L);
        req.setCheckIn(LocalDate.now().plusDays(20));
        req.setCheckOut(LocalDate.now().plusDays(25));
        req.setNumGuests(2);
        req.setTotalPrice(new BigDecimal("400.00"));

        mockMvc.perform(post("/api/reservations/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(req, req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getReservationWithDetails_Returns200() throws Exception {
        when(reservationService.getReservationWithDetails(1L)).thenReturn(sample);

        mockMvc.perform(get("/api/reservations/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
