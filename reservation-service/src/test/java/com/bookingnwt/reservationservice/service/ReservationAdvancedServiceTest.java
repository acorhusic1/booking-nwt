package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.ReservationRequestDTO;
import com.bookingnwt.reservationservice.dto.ReservationResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.ReservationMapper;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.model.ReservationStatus;
import com.bookingnwt.reservationservice.repository.CancellationPolicyRepository;
import com.bookingnwt.reservationservice.repository.PromoCodeRepository;
import com.bookingnwt.reservationservice.client.PropertyAvailabilityGateway;
import com.bookingnwt.reservationservice.repository.ReservationRepository;
import com.bookingnwt.reservationservice.service.impl.ReservationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationAdvancedServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private CancellationPolicyRepository cancellationPolicyRepository;
    @Mock
    private PromoCodeRepository promoCodeRepository;
    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private PropertyAvailabilityGateway propertyAvailabilityGateway;

    @InjectMocks
    private ReservationServiceImpl service;

    private Reservation entity;
    private ReservationResponseDTO dto;

    @BeforeEach
    void setUp() {
        entity = new Reservation();
        entity.setId(1L);
        entity.setGuestId(10L);
        entity.setHostId(20L);
        entity.setPropertyId(30L);
        entity.setCheckIn(LocalDate.of(2026, 6, 1));
        entity.setCheckOut(LocalDate.of(2026, 6, 5));
        entity.setNumGuests(2);
        entity.setTotalPrice(new BigDecimal("400.00"));
        entity.setStatus(ReservationStatus.CREATED);

        dto = new ReservationResponseDTO();
        dto.setId(1L);
        dto.setGuestId(10L);
        dto.setHostId(20L);
        dto.setPropertyId(30L);
        dto.setCheckIn(entity.getCheckIn());
        dto.setCheckOut(entity.getCheckOut());
        dto.setNumGuests(2);
        dto.setTotalPrice(new BigDecimal("400.00"));
        dto.setStatus(ReservationStatus.CREATED);

        // Use real ObjectMapper for patch tests via reflection-friendly @InjectMocks
        // we'll set it through field if needed; for unit tests below we use a fresh service
    }

    @Test
    void patchReservation_replaceField_persistsChange() throws Exception {
        ObjectMapper realMapper = JsonMapper.builder().findAndAddModules().build();
        ReservationServiceImpl real = new ReservationServiceImpl(
                reservationRepository, cancellationPolicyRepository,
                promoCodeRepository, reservationMapper, realMapper,
                propertyAvailabilityGateway);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(reservationMapper.toResponseDTO(any(Reservation.class))).thenReturn(dto);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(entity);

        var patch = realMapper.readTree("[{\"op\":\"replace\",\"path\":\"/numGuests\",\"value\":4}]");
        ReservationResponseDTO result = real.patchReservation(1L, patch);

        assertThat(result).isNotNull();
        assertThat(entity.getNumGuests()).isEqualTo(4);
    }

    @Test
    void patchReservation_invalidOp_throwsIllegalArgument() throws Exception {
        ObjectMapper realMapper = JsonMapper.builder().findAndAddModules().build();
        ReservationServiceImpl real = new ReservationServiceImpl(
                reservationRepository, cancellationPolicyRepository,
                promoCodeRepository, reservationMapper, realMapper,
                propertyAvailabilityGateway);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(entity));

        var truelyBad = realMapper.readTree("[{\"op\":\"unknown_op\",\"path\":\"/x\",\"value\":1}]");
        assertThatThrownBy(() -> real.patchReservation(1L, truelyBad))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void patchReservation_notFound_throws() throws Exception {
        ObjectMapper realMapper = JsonMapper.builder().findAndAddModules().build();
        ReservationServiceImpl real = new ReservationServiceImpl(
                reservationRepository, cancellationPolicyRepository,
                promoCodeRepository, reservationMapper, realMapper,
                propertyAvailabilityGateway);

        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        var patch = realMapper.readTree("[{\"op\":\"replace\",\"path\":\"/numGuests\",\"value\":4}]");

        assertThatThrownBy(() -> real.patchReservation(99L, patch))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getReservationsByGuestPaged_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 5);
        when(reservationRepository.findByGuestId(eq(10L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(reservationMapper.toResponseDTO(entity)).thenReturn(dto);

        Page<ReservationResponseDTO> result = service.getReservationsByGuestPaged(10L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getReservationsByGuestAndDateRange_invalid_throws() {
        assertThatThrownBy(() ->
                service.getReservationsByGuestAndDateRange(10L,
                        LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getReservationsByGuestAndDateRange_returnsList() {
        when(reservationRepository.findByGuestAndDateRange(eq(10L),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(entity));
        when(reservationMapper.toResponseDTO(entity)).thenReturn(dto);

        var result = service.getReservationsByGuestAndDateRange(10L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        assertThat(result).hasSize(1);
    }

    @Test
    void getHostRevenue_nullCoercedToZero() {
        when(reservationRepository.sumConfirmedRevenueByHost(20L)).thenReturn(null);
        assertThat(service.getHostRevenue(20L)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getHostRevenue_returnsValue() {
        when(reservationRepository.sumConfirmedRevenueByHost(20L)).thenReturn(new BigDecimal("999.99"));
        assertThat(service.getHostRevenue(20L)).isEqualByComparingTo("999.99");
    }

    @Test
    void batchCreate_emptyList_throws() {
        assertThatThrownBy(() -> service.batchCreate(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void batchCreate_savesAll() {
        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setGuestId(10L);
        req.setHostId(20L);
        req.setPropertyId(30L);
        req.setCheckIn(LocalDate.of(2026, 6, 1));
        req.setCheckOut(LocalDate.of(2026, 6, 5));
        req.setNumGuests(2);
        req.setTotalPrice(new BigDecimal("400.00"));

        when(reservationMapper.toEntity(any(ReservationRequestDTO.class))).thenReturn(entity);
        when(reservationRepository.saveAll(anyList())).thenReturn(List.of(entity, entity));
        when(reservationMapper.toResponseDTO(entity)).thenReturn(dto);

        var result = service.batchCreate(List.of(req, req));
        assertThat(result).hasSize(2);
    }

    @Test
    void getReservationWithDetails_notFound_throws() {
        when(reservationRepository.findWithDetailsById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getReservationWithDetails(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getReservationWithDetails_returnsDto() {
        when(reservationRepository.findWithDetailsById(1L)).thenReturn(Optional.of(entity));
        when(reservationMapper.toResponseDTO(entity)).thenReturn(dto);
        assertThat(service.getReservationWithDetails(1L).getId()).isEqualTo(1L);
    }
}
