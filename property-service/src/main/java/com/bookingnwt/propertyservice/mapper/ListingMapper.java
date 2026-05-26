package com.bookingnwt.propertyservice.mapper;

import com.bookingnwt.propertyservice.dto.ListingRequest;
import com.bookingnwt.propertyservice.dto.ListingResponse;
import com.bookingnwt.propertyservice.model.Listing;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {

    public Listing toEntity(ListingRequest request) {
        return new Listing(
            request.getPropertyId(),
            request.getHostId(),
            request.getPricePerNight()
        );
    }

    public ListingResponse toResponse(Listing listing) {
        return new ListingResponse(
            listing.getId(),
            listing.getPropertyId(),
            listing.getHostId(),
            listing.getPricePerNight(),
            listing.getIsCancelled(),
            listing.getCreatedAt()
        );
    }
}
