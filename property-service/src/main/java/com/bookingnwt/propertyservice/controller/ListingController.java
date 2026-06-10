package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.ListingRequest;
import com.bookingnwt.propertyservice.dto.ListingResponse;
import com.bookingnwt.propertyservice.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ListingResponse> createListing(@Valid @RequestBody ListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.createListing(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListing(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListingById(id));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ListingResponse>> getListingsByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(listingService.getListingsByProperty(propertyId));
    }

    // K8 fix — gasenje/paljenje listinga je HOST/ADMIN operacija. GUEST rola je
    // ovdje bila ostavljena za stari (pogresan) flow gdje je otkazivanje
    // rezervacije gasilo sve listinge property-ja (K3 — uklonjeno).
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<ListingResponse> toggleCancelStatus(
            @PathVariable Long id,
            @RequestParam Boolean isCancelled) {
        return ResponseEntity.ok(listingService.updateCancelStatus(id, isCancelled));
    }

    @PatchMapping("/property/{propertyId}/cancel")
    @PreAuthorize("hasAnyRole('HOST', 'ADMIN')")
    public ResponseEntity<Void> cancelListingsByProperty(
            @PathVariable Long propertyId,
            @RequestParam Boolean isCancelled) {
        listingService.cancelListingsByProperty(propertyId, isCancelled);
        return ResponseEntity.ok().build();
    }
}
