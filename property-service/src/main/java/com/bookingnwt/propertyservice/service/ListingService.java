package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.ListingRequest;
import com.bookingnwt.propertyservice.dto.ListingResponse;

import java.util.List;

public interface ListingService {
    ListingResponse createListing(ListingRequest request);
    ListingResponse getListingById(Long id);
    List<ListingResponse> getListingsByProperty(Long propertyId);
    ListingResponse updateCancelStatus(Long id, Boolean isCancelled);
    void cancelListingsByProperty(Long propertyId, Boolean isCancelled);
}
