package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.PropertyPatchRequest;
import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.PropertyMapper;
import com.bookingnwt.propertyservice.model.Amenity;
import com.bookingnwt.propertyservice.model.Property;
import com.bookingnwt.propertyservice.repository.AmenityRepository;
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
    private final AmenityRepository amenityRepository;

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PropertyResponse> getAllProperties(org.springframework.data.domain.Pageable pageable) {
        // F2 — javna lista vraca samo APPROVED smjestaje. PENDING/REJECTED su sakriveni
        // od gostiju dok admin ne odobri. Legacy seed data (null moderationStatus)
        // se tretira kao APPROVED radi backwards compat.
        return propertyRepository.findApprovedForPublic(pageable)
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
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesInBounds(java.math.BigDecimal minLat, java.math.BigDecimal maxLat,
                                                        java.math.BigDecimal minLng, java.math.BigDecimal maxLng) {
        return propertyRepository.findInBounds(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(propertyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void registerView(Long id) {
        // F11 — namjerno bez existsById provjere: nepostojeci ID samo ne
        // azurira nijedan red (jeftinije od dodatnog SELECT-a po pregledu)
        propertyRepository.incrementViewCount(id);
    }

    @Override
    public PropertyResponse createProperty(PropertyRequest request) {
        Property property = propertyMapper.toEntity(request);
        property.setAvailable(true);
        attachAmenities(property, request.getAmenityIds());
        Property saved = propertyRepository.save(property);
        return propertyMapper.toResponse(saved);
    }

    // F1 — host izabere amenity ID-eve u modalu; ovdje ih dohvatimo iz repo
    // i postavimo na entity (ManyToMany sa amenity tabelom)
    private void attachAmenities(Property property, java.util.Set<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            property.setAmenities(new java.util.HashSet<>());
            return;
        }
        java.util.Set<Amenity> amenities = new java.util.HashSet<>(amenityRepository.findAllById(amenityIds));
        property.setAmenities(amenities);
    }

    @Override
    public PropertyResponse updateProperty(Long id, PropertyRequest request) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + id + " nije pronađena"));
        propertyMapper.updateEntity(request, property);
        attachAmenities(property, request.getAmenityIds());
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
        if (request.getPropertyType() != null) property.setPropertyType(request.getPropertyType());
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

    // F2 — moderacija
    @Override
    public PropertyResponse updateModerationStatus(Long id, String status) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nekretnina sa ID " + id + " nije pronađena"));
        property.setModerationStatus(status);
        return propertyMapper.toResponse(propertyRepository.save(property));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyResponse> getByModerationStatus(String status) {
        return propertyRepository.findByModerationStatus(status)
                .stream()
                .map(propertyMapper::toResponse)
                .toList();
    }
}
