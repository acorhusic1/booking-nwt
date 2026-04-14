package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.PropertyImageRequest;
import com.bookingnwt.propertyservice.dto.PropertyImageResponse;
import com.bookingnwt.propertyservice.service.PropertyImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/images")
@RequiredArgsConstructor
public class PropertyImageController {

    private final PropertyImageService imageService;

    @GetMapping
    public ResponseEntity<List<PropertyImageResponse>> getImages(@PathVariable Long propertyId) {
        return ResponseEntity.ok(imageService.getImagesByPropertyId(propertyId));
    }

    @PostMapping
    public ResponseEntity<PropertyImageResponse> addImage(@PathVariable Long propertyId,
                                                          @Valid @RequestBody PropertyImageRequest request) {
        PropertyImageResponse created = imageService.addImage(propertyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long propertyId,
                                            @PathVariable Long imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
