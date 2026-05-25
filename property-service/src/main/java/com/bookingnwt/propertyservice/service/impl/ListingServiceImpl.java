package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.ListingRequest;
import com.bookingnwt.propertyservice.dto.ListingResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.ListingMapper;
import com.bookingnwt.propertyservice.model.Listing;
import com.bookingnwt.propertyservice.repository.ListingRepository;
import com.bookingnwt.propertyservice.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;
    private final ListingMapper listingMapper;

    @Override
    public ListingResponse createListing(ListingRequest request) {
        Listing listing = listingMapper.toEntity(request);
        Listing saved = listingRepository.save(listing);
        return listingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ListingResponse getListingById(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing nije pronadjen."));
        return listingMapper.toResponse(listing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListingResponse> getListingsByProperty(Long propertyId) {
        return listingRepository.findByPropertyId(propertyId).stream()
                .map(listingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ListingResponse updateCancelStatus(Long id, Boolean isCancelled) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing nije pronadjen."));
        listing.setIsCancelled(isCancelled);
        Listing saved = listingRepository.save(listing);
        return listingMapper.toResponse(saved);
    }

    @Override
    public void cancelListingsByProperty(Long propertyId, Boolean isCancelled) {
        List<Listing> listings = listingRepository.findByPropertyId(propertyId);
        listings.forEach(listing -> listing.setIsCancelled(isCancelled));
        listingRepository.saveAll(listings);
    }
}
