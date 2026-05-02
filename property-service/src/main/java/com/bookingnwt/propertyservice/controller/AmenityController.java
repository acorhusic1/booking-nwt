package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.AmenityRequest;
import com.bookingnwt.propertyservice.dto.AmenityResponse;
import com.bookingnwt.propertyservice.service.AmenityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping
    public ResponseEntity<List<AmenityResponse>> getAllAmenities() {
        return ResponseEntity.ok(amenityService.getAllAmenities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityResponse> getAmenityById(@PathVariable Long id) {
        return ResponseEntity.ok(amenityService.getAmenityById(id));
    }

    @PostMapping
    public ResponseEntity<AmenityResponse> createAmenity(@Valid @RequestBody AmenityRequest request) {
        AmenityResponse created = amenityService.createAmenity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
