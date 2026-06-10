package com.bookingnwt.reservationservice.service;

import com.bookingnwt.reservationservice.dto.ProblemReportRequestDTO;
import com.bookingnwt.reservationservice.dto.ProblemReportResponseDTO;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.ProblemReportMapper;
import com.bookingnwt.reservationservice.model.ProblemReport;
import com.bookingnwt.reservationservice.model.ProblemReportStatus;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.publisher.ReservationEventPublisher;
import com.bookingnwt.reservationservice.repository.ProblemReportRepository;
import com.bookingnwt.reservationservice.repository.ReservationRepository;
import com.bookingnwt.reservationservice.service.impl.ProblemReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemReportServiceTest {

    @Mock
    private ProblemReportRepository reportRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ProblemReportMapper reportMapper;
    @Mock
    private ReservationEventPublisher eventPublisher;

    @InjectMocks
    private ProblemReportServiceImpl reportService;

    private ProblemReport report;
    private Reservation reservation;
    private ProblemReportRequestDTO requestDTO;
    private ProblemReportResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        reservation.setId(1L);

        report = new ProblemReport();
        report.setId(1L);
        report.setReservation(reservation);
        report.setReporterId(10L);
        report.setCategory("Čistoća");
        report.setDescription("Soba nije bila čista");
        report.setStatus(ProblemReportStatus.REPORTED);
        report.setReportedAt(LocalDateTime.now());

        requestDTO = new ProblemReportRequestDTO();
        requestDTO.setReservationId(1L);
        requestDTO.setReporterId(10L);
        requestDTO.setCategory("Čistoća");
        requestDTO.setDescription("Soba nije bila čista");

        responseDTO = new ProblemReportResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setReservationId(1L);
        responseDTO.setReporterId(10L);
        responseDTO.setCategory("Čistoća");
        responseDTO.setDescription("Soba nije bila čista");
        responseDTO.setStatus(ProblemReportStatus.REPORTED);
    }

    @Test
    void createReport_Success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reportMapper.toEntity(requestDTO)).thenReturn(report);
        when(reportRepository.save(any(ProblemReport.class))).thenReturn(report);
        when(reportMapper.toResponseDTO(report)).thenReturn(responseDTO);

        ProblemReportResponseDTO result = reportService.createReport(requestDTO);

        assertNotNull(result);
        assertEquals("Čistoća", result.getCategory());
        verify(reportRepository).save(any(ProblemReport.class));
    }

    @Test
    void createReport_ReservationNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());
        requestDTO.setReservationId(99L);

        assertThrows(ResourceNotFoundException.class, () -> reportService.createReport(requestDTO));
    }

    @Test
    void getReportById_Success() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportMapper.toResponseDTO(report)).thenReturn(responseDTO);

        ProblemReportResponseDTO result = reportService.getReportById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getReportById_NotFound() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> reportService.getReportById(99L));
    }

    @Test
    void getAllReports_Success() {
        when(reportRepository.findAll()).thenReturn(List.of(report));
        when(reportMapper.toResponseDTO(report)).thenReturn(responseDTO);

        List<ProblemReportResponseDTO> result = reportService.getAllReports();

        assertEquals(1, result.size());
    }

    @Test
    void getReportsByReservation_Success() {
        when(reportRepository.findByReservationId(1L)).thenReturn(List.of(report));
        when(reportMapper.toResponseDTO(report)).thenReturn(responseDTO);

        List<ProblemReportResponseDTO> result = reportService.getReportsByReservation(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getReportsByReporter_Success() {
        when(reportRepository.findByReporterId(10L)).thenReturn(List.of(report));
        when(reportMapper.toResponseDTO(report)).thenReturn(responseDTO);

        List<ProblemReportResponseDTO> result = reportService.getReportsByReporter(10L);

        assertEquals(1, result.size());
    }

    @Test
    void updateStatus_Success() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(ProblemReport.class))).thenReturn(report);

        responseDTO.setStatus(ProblemReportStatus.RESOLVED);
        when(reportMapper.toResponseDTO(report)).thenReturn(responseDTO);

        ProblemReportResponseDTO result = reportService.updateStatus(1L, ProblemReportStatus.RESOLVED);

        assertNotNull(result);
        assertEquals(ProblemReportStatus.RESOLVED, result.getStatus());
    }

    @Test
    void deleteReport_Success() {
        when(reportRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reportRepository).deleteById(1L);

        assertDoesNotThrow(() -> reportService.deleteReport(1L));
        verify(reportRepository).deleteById(1L);
    }

    @Test
    void deleteReport_NotFound() {
        when(reportRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> reportService.deleteReport(99L));
    }
}
