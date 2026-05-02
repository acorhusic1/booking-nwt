package com.bookingnwt.userservice.controller;

import com.bookingnwt.userservice.dto.UserPreferenceRequest;
import com.bookingnwt.userservice.dto.UserPreferenceResponse;
import com.bookingnwt.userservice.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<UserPreferenceResponse> getPreference(@PathVariable Long userId) {
        return ResponseEntity.ok(preferenceService.getPreferenceByUserId(userId));
    }

    @PutMapping
    public ResponseEntity<UserPreferenceResponse> createOrUpdatePreference(
            @PathVariable Long userId,
            @Valid @RequestBody UserPreferenceRequest request) {
        request.setUserId(userId);
        return ResponseEntity.ok(preferenceService.createOrUpdatePreference(request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePreference(@PathVariable Long userId) {
        preferenceService.deletePreference(userId);
        return ResponseEntity.noContent().build();
    }
}
