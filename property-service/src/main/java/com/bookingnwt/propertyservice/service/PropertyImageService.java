package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.PropertyImageRequest;
import com.bookingnwt.propertyservice.dto.PropertyImageResponse;

import java.util.List;

public interface PropertyImageService {
    List<PropertyImageResponse> getImagesByPropertyId(Long propertyId);
    PropertyImageResponse addImage(Long propertyId, PropertyImageRequest request);
    void deleteImage(Long imageId);
}
