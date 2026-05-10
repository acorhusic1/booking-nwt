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
    PropertyResponse createProperty(PropertyRequest request);
    PropertyResponse updateProperty(Long id, PropertyRequest request);
    PropertyResponse patchProperty(Long id, PropertyPatchRequest request);
    List<PropertyResponse> batchCreateProperties(List<PropertyRequest> requests);
    void deleteProperty(Long id);
}
