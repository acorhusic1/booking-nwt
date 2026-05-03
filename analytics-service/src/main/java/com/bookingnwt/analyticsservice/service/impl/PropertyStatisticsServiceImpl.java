package com.bookingnwt.analyticsservice.service.impl;

import com.bookingnwt.analyticsservice.dto.PropertyStatisticsRequestDTO;
import com.bookingnwt.analyticsservice.dto.PropertyStatisticsResponseDTO;
import com.bookingnwt.analyticsservice.exception.ResourceNotFoundException;
import com.bookingnwt.analyticsservice.mapper.PropertyStatisticsMapper;
import com.bookingnwt.analyticsservice.model.PropertyStatistics;
import com.bookingnwt.analyticsservice.repository.PropertyStatisticsRepository;
import com.bookingnwt.analyticsservice.service.PropertyStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyStatisticsServiceImpl implements PropertyStatisticsService {

    private final PropertyStatisticsRepository repository;
    private final PropertyStatisticsMapper mapper;

    @Override
    public PropertyStatisticsResponseDTO createStatistics(PropertyStatisticsRequestDTO dto) {
        PropertyStatistics entity = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(entity));
    }

    @Override
    public PropertyStatisticsResponseDTO getStatisticsById(Long id) {
        PropertyStatistics entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statistika sa ID " + id + " nije pronađena"));
        return mapper.toDTO(entity);
    }

    @Override
    public List<PropertyStatisticsResponseDTO> getAllStatistics() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public org.springframework.data.domain.Page<PropertyStatisticsResponseDTO> getAllStatisticsPaginated(org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    @Override
    public List<PropertyStatisticsResponseDTO> createStatisticsBatch(List<PropertyStatisticsRequestDTO> dtos) {
        List<PropertyStatistics> entities = dtos.stream().map(mapper::toEntity).collect(Collectors.toList());
        return repository.saveAll(entities).stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PropertyStatisticsResponseDTO> getStatisticsByPropertyId(Long propertyId) {
        return repository.findByPropertyId(propertyId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyStatisticsResponseDTO> getStatisticsByHostId(Long hostId) {
        return repository.findByHostId(hostId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyStatisticsResponseDTO> getStatisticsByHostIdAndPeriod(Long hostId, Integer year, Integer month) {
        return repository.findByHostIdAndYearAndMonth(hostId, year, month).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStatistics(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Statistika sa ID " + id + " nije pronađena");
        }
        repository.deleteById(id);
    }
}
