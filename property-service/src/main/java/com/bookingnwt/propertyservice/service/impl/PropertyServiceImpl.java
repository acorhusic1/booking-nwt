package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.PropertyMapper;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.repository.PropertyRepository;
import com.bookingnwt.propertyservice.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll()
                .stream()
                .map(propertyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + id + " nije pronađena"));
        return propertyMapper.toResponse(property);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByHostId(Long hostId) {
        return propertyRepository.findByHostId(hostId)
                .stream()
                .map(propertyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByCity(String city) {
        return propertyRepository.findByCityAndIsActiveTrue(city)
                .stream()
                .map(propertyMapper::toResponse)
                .toList();
    }

    @Override
    public PropertyResponse createProperty(PropertyRequest request) {
        Property property = propertyMapper.toEntity(request);
        Property saved = propertyRepository.save(property);
        return propertyMapper.toResponse(saved);
    }

    @Override
    public PropertyResponse updateProperty(Long id, PropertyRequest request) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + id + " nije pronađena"));
        propertyMapper.updateEntity(request, property);
        Property updated = propertyRepository.save(property);
        return propertyMapper.toResponse(updated);
    }

    @Override
    public void deleteProperty(Long id) {
        if (!propertyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Nekretnina sa ID " + id + " nije pronađena");
        }
        propertyRepository.deleteById(id);
    }
}
