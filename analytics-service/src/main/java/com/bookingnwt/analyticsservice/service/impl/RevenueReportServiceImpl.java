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
