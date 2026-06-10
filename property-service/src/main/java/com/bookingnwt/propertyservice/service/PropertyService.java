package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.PropertyPatchRequest;
import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PropertyService {
    Page<PropertyResponse> getAllProperties(Pageable pageable);
    PropertyResponse getPropertyById(Long id);
    List<PropertyResponse> getPropertiesByHostId(Long hostId);
    List<PropertyResponse> getPropertiesByCity(String city);
    List<PropertyResponse> getAvailableProperties(String city, java.time.LocalDate startDate, java.time.LocalDate endDate);
    // F18 — smjestaji u vidljivom dijelu mape (dinamicko ucitavanje)
    List<PropertyResponse> getPropertiesInBounds(java.math.BigDecimal minLat, java.math.BigDecimal maxLat,
                                                 java.math.BigDecimal minLng, java.math.BigDecimal maxLng);
    // F11 — broj pregleda oglasa (zove frontend pri otvaranju detalja)
    void registerView(Long id);
    PropertyResponse createProperty(PropertyRequest request);
    PropertyResponse updateProperty(Long id, PropertyRequest request);
    PropertyResponse patchProperty(Long id, PropertyPatchRequest request);
    List<PropertyResponse> batchCreateProperties(List<PropertyRequest> requests);
    void deleteProperty(Long id);

    // F2 — moderacija
    PropertyResponse updateModerationStatus(Long id, String status);
    List<PropertyResponse> getByModerationStatus(String status);
}
