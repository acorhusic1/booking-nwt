package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.AmenityRequest;
import com.bookingnwt.propertyservice.dto.AmenityResponse;

import java.util.List;

public interface AmenityService {
    List<AmenityResponse> getAllAmenities();
    AmenityResponse getAmenityById(Long id);
    AmenityResponse createAmenity(AmenityRequest request);
}
