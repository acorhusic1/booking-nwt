package com.bookingnwt.reservationservice.service.impl;

import com.bookingnwt.reservationservice.dto.ProblemReportRequestDTO;
import com.bookingnwt.reservationservice.dto.ProblemReportResponseDTO;
import com.bookingnwt.reservationservice.events.ProblemReportedEvent;
import com.bookingnwt.reservationservice.exception.ResourceNotFoundException;
import com.bookingnwt.reservationservice.mapper.ProblemReportMapper;
import com.bookingnwt.reservationservice.model.ProblemReport;
import com.bookingnwt.reservationservice.model.ProblemReportStatus;
import com.bookingnwt.reservationservice.model.Reservation;
import com.bookingnwt.reservationservice.publisher.ReservationEventPublisher;
import com.bookingnwt.reservationservice.repository.ProblemReportRepository;
import com.bookingnwt.reservationservice.repository.ReservationRepository;
import com.bookingnwt.reservationservice.service.ProblemReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemReportServiceImpl implements ProblemReportService {

    private final ProblemReportRepository reportRepository;
    private final ReservationRepository reservationRepository;
    private final ProblemReportMapper reportMapper;
    private final ReservationEventPublisher eventPublisher;

    @Override
    public ProblemReportResponseDTO createReport(ProblemReportRequestDTO dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rezervacija nije pronađena sa ID: " + dto.getReservationId()));

        ProblemReport report = reportMapper.toEntity(dto);
        report.setReservation(reservation);
        report.setStatus(ProblemReportStatus.REPORTED);
        report.setReportedAt(LocalDateTime.now());

        ProblemReport saved = reportRepository.save(report);

        // F17 — notifikacija hostu o prijavljenom problemu (preko notification-service)
        try {
            eventPublisher.publishProblemReported(new ProblemReportedEvent(
                    saved.getId(), reservation.getId(), reservation.getPropertyId(),
                    reservation.getHostId(), saved.getReporterId(),
                    saved.getCategory(), saved.getDescription(),
                    LocalDateTime.now(), "PROBLEM_REPORTED"));
        } catch (Exception e) {
            log.warn("⚠️ ProblemReportedEvent publish nije uspio za prijavu {}: {}",
                    saved.getId(), e.getMessage());
        }

        return reportMapper.toResponseDTO(saved);
    }

    @Override
    public ProblemReportResponseDTO getReportById(Long id) {
        ProblemReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProblemReport nije pronađen sa ID: " + id));
        return reportMapper.toResponseDTO(report);
    }

    @Override
    public List<ProblemReportResponseDTO> getAllReports() {
        return reportRepository.findAll().stream()
                .map(reportMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProblemReportResponseDTO> getReportsByReservation(Long reservationId) {
        return reportRepository.findByReservationId(reservationId).stream()
                .map(reportMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProblemReportResponseDTO> getReportsByReporter(Long reporterId) {
        return reportRepository.findByReporterId(reporterId).stream()
                .map(reportMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProblemReportResponseDTO> getReportsByHost(Long hostId) {
        return reportRepository.findByReservationHostId(hostId).stream()
                .map(reportMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProblemReportResponseDTO updateStatus(Long id, ProblemReportStatus status) {
        ProblemReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProblemReport nije pronađen sa ID: " + id));
        report.setStatus(status);
        if (status == ProblemReportStatus.RESOLVED || status == ProblemReportStatus.CLOSED) {
            report.setResolvedAt(LocalDateTime.now());
        }
        ProblemReport saved = reportRepository.save(report);
        return reportMapper.toResponseDTO(saved);
    }

    @Override
    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("ProblemReport nije pronađen sa ID: " + id);
        }
        reportRepository.deleteById(id);
    }
}
