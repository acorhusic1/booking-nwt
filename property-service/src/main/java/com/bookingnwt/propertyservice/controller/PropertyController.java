package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByHostId(@PathVariable Long hostId) {
        return ResponseEntity.ok(propertyService.getPropertiesByHostId(hostId));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByCity(@PathVariable String city) {
        return ResponseEntity.ok(propertyService.getPropertiesByCity(city));
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(@Valid @RequestBody PropertyRequest request) {
        PropertyResponse created = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(@PathVariable Long id,
                                                           @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }
}
