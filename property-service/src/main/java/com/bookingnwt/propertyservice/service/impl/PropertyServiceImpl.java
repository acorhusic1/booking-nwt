package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.PropertyPatchRequest;
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
    public org.springframework.data.domain.Page<PropertyResponse> getAllProperties(org.springframework.data.domain.Pageable pageable) {
        return propertyRepository.findAll(pageable)
                .map(propertyMapper::toResponse);
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
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAvailableProperties(String query, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        // BUG 4 — query matches city ILI country; ako je prazan/null, vrati sve dostupne
        String safe = (query == null) ? "" : query.trim();
        return propertyRepository.findAvailableProperties(safe, startDate, endDate)
                .stream()
                .map(propertyMapper::toResponse)
                .toList();
    }

    @Override
    public PropertyResponse createProperty(PropertyRequest request) {
        Property property = propertyMapper.toEntity(request);
        property.setAvailable(true);
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

    /**
     * PATCH — parcijalni update. Samo non-null polja se ažuriraju.
     */
    @Override
    public PropertyResponse patchProperty(Long id, PropertyPatchRequest request) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + id + " nije pronađena"));

        if (request.getName() != null) property.setName(request.getName());
        if (request.getDescription() != null) property.setDescription(request.getDescription());
        if (request.getAddress() != null) property.setAddress(request.getAddress());
        if (request.getCity() != null) property.setCity(request.getCity());
        if (request.getCountry() != null) property.setCountry(request.getCountry());
        if (request.getLatitude() != null) property.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) property.setLongitude(request.getLongitude());
        if (request.getMaxGuests() != null) property.setMaxGuests(request.getMaxGuests());
        if (request.getIsActive() != null) property.setIsActive(request.getIsActive());
        if (request.getAvailable() != null) property.setAvailable(request.getAvailable());

        Property updated = propertyRepository.save(property);
        return propertyMapper.toResponse(updated);
    }

    /**
     * Batch unos — kreira više nekretnina odjednom koristeći saveAll().
     */
    @Override
    public List<PropertyResponse> batchCreateProperties(List<PropertyRequest> requests) {
        List<Property> properties = requests.stream()
                .map(req -> {
                    Property p = propertyMapper.toEntity(req);
                    p.setAvailable(true);
                    return p;
                })
                .toList();

        List<Property> saved = propertyRepository.saveAll(properties);
        return saved.stream()
                .map(propertyMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteProperty(Long id) {
        if (!propertyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Nekretnina sa ID " + id + " nije pronađena");
        }
        propertyRepository.deleteById(id);
    }
}
