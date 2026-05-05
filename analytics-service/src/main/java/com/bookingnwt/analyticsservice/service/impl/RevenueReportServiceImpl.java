package com.bookingnwt.analyticsservice.service.impl;

import com.bookingnwt.analyticsservice.dto.RevenueReportRequestDTO;
import com.bookingnwt.analyticsservice.dto.RevenueReportResponseDTO;
import com.bookingnwt.analyticsservice.exception.ResourceNotFoundException;
import com.bookingnwt.analyticsservice.mapper.RevenueReportMapper;
import com.bookingnwt.analyticsservice.model.RevenueReport;
import com.bookingnwt.analyticsservice.repository.RevenueReportRepository;
import com.bookingnwt.analyticsservice.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueReportServiceImpl implements RevenueReportService {

    private final RevenueReportRepository repository;
    private final RevenueReportMapper mapper;
    private final com.bookingnwt.analyticsservice.client.UserClient userClient;

    @Override
    public RevenueReportResponseDTO createReport(RevenueReportRequestDTO dto) {
        RevenueReport entity = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(entity));
    }

    @Override
    public RevenueReportResponseDTO getReportById(Long id) {
        RevenueReport entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Izvještaj sa ID " + id + " nije pronađen"));
        return mapper.toDTO(entity);
    }

    @Override
    public List<RevenueReportResponseDTO> getAllReports() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenueReportResponseDTO> getReportsByHostId(Long hostId) {
        return repository.findByHostId(hostId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "userService", fallbackMethod = "getDetailedHostReportFallback")
    public com.bookingnwt.analyticsservice.dto.DetailedHostReportDto getDetailedHostReport(Long hostId) {
        // Synchronous inter-service call to user-service using Feign
        com.bookingnwt.analyticsservice.dto.UserDto hostDetails = userClient.getUserById(hostId);
        
        List<RevenueReportResponseDTO> reports = getReportsByHostId(hostId);
        
        return com.bookingnwt.analyticsservice.dto.DetailedHostReportDto.builder()
                .hostDetails(hostDetails)
                .reports(reports)
                .build();
    }

    public com.bookingnwt.analyticsservice.dto.DetailedHostReportDto getDetailedHostReportFallback(Long hostId, Throwable throwable) {
        // Fallback when user-service is down or fails
        com.bookingnwt.analyticsservice.dto.UserDto fallbackUser = com.bookingnwt.analyticsservice.dto.UserDto.builder()
                .id(hostId)
                .firstName("Nepoznat (Fallback)")
                .lastName("Korisnik")
                .email("nedostupno@fallback.com")
                .role("HOST")
                .build();
                
        List<RevenueReportResponseDTO> reports = getReportsByHostId(hostId);
        
        return com.bookingnwt.analyticsservice.dto.DetailedHostReportDto.builder()
                .hostDetails(fallbackUser)
                .reports(reports)
                .build();
    }

    @Override
    public List<RevenueReportResponseDTO> getReportsByHostIdAndYear(Long hostId, Integer year) {
        return repository.findByHostIdAndYear(hostId, year).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenueReportResponseDTO> getReportsByPeriod(Integer year, Integer month) {
        return repository.findByYearAndMonth(year, month).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReport(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Izvještaj sa ID " + id + " nije pronađen");
        }
        repository.deleteById(id);
    }
}
