package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.AmenityRequest;
import com.bookingnwt.propertyservice.dto.AmenityResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.AmenityMapper;
import com.bookingnwt.propertyservice.model.Amenity;
import com.bookingnwt.propertyservice.repository.AmenityRepository;
import com.bookingnwt.propertyservice.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll()
                .stream()
                .map(amenityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AmenityResponse getAmenityById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sadržaj sa ID " + id + " nije pronađen"));
        return amenityMapper.toResponse(amenity);
    }

    @Override
    public AmenityResponse createAmenity(AmenityRequest request) {
        Amenity amenity = amenityMapper.toEntity(request);
        Amenity saved = amenityRepository.save(amenity);
        return amenityMapper.toResponse(saved);
    }
}
