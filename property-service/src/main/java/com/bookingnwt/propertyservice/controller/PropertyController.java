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

    @org.springframework.beans.factory.annotation.Value("${server.port}")
    private String port;

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<PropertyResponse>> getAllProperties(org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(propertyService.getAllProperties(pageable));
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

    @GetMapping("/search")
    public ResponseEntity<List<PropertyResponse>> searchAvailableProperties(
            @RequestParam String city,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        return ResponseEntity.ok(propertyService.getAvailableProperties(city, startDate, endDate));
    }

    @GetMapping("/test")
    @org.springframework.security.access.prepost.PreAuthorize("permitAll()")
    public ResponseEntity<String> testLoadBalancing() {
        // Jednostavan endpoint za testiranje load balancinga
        try {
            String instanceId = java.net.InetAddress.getLocalHost().getHostName();
            return ResponseEntity.ok("Property Service Instance: " + instanceId + " (Port: " + port + ") - " + java.time.LocalDateTime.now());
        } catch (Exception e) {
            return ResponseEntity.ok("Property Service Instance: unknown (Port: " + port + ") - " + java.time.LocalDateTime.now());
        }
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> createProperty(@Valid @RequestBody PropertyRequest request) {
        PropertyResponse created = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> updateProperty(@PathVariable Long id,
                                                           @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }
}
