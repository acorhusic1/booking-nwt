package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;

import java.util.List;

public interface PropertyService {
    List<PropertyResponse> getAllProperties();
    PropertyResponse getPropertyById(Long id);
    List<PropertyResponse> getPropertiesByHostId(Long hostId);
    List<PropertyResponse> getPropertiesByCity(String city);
    PropertyResponse createProperty(PropertyRequest request);
    PropertyResponse updateProperty(Long id, PropertyRequest request);
    void deleteProperty(Long id);
}
