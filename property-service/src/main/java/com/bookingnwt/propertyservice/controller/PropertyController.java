package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.PropertyPatchRequest;
import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @Value("${server.port}")
    private String port;

    // ===================== PUBLIC GET ENDPOINTS =====================

    @GetMapping
    public ResponseEntity<Page<PropertyResponse>> getAllProperties(Pageable pageable) {
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(propertyService.getAvailableProperties(city, startDate, endDate));
    }

    @GetMapping("/test")
    public ResponseEntity<String> testLoadBalancing() {
        try {
            String instanceId = InetAddress.getLocalHost().getHostName();
            return ResponseEntity.ok("Property Service Instance: " + instanceId
                    + " (Port: " + port + ") - " + LocalDateTime.now());
        } catch (Exception e) {
            return ResponseEntity.ok("Property Service Instance: unknown (Port: " + port + ") - " + LocalDateTime.now());
        }
    }

    // ===================== PROTECTED ENDPOINTS =====================

    @PostMapping
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> createProperty(@Valid @RequestBody PropertyRequest request) {
        PropertyResponse created = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> updateProperty(@PathVariable Long id,
                                                           @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }

    /**
     * PATCH — parcijalni update nekretnine. Samo poslana (non-null) polja se ažuriraju.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> patchProperty(@PathVariable Long id,
                                                          @RequestBody PropertyPatchRequest request) {
        return ResponseEntity.ok(propertyService.patchProperty(id, request));
    }

    /**
     * Batch unos — kreira više nekretnina odjednom.
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PropertyResponse>> batchCreateProperties(
            @Valid @RequestBody List<PropertyRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyService.batchCreateProperties(requests));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }
}
