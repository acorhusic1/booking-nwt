package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.CancellationPolicyRequestDTO;
import com.bookingnwt.reservationservice.dto.CancellationPolicyResponseDTO;
import com.bookingnwt.reservationservice.service.CancellationPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cancellation-policies")
@RequiredArgsConstructor
public class CancellationPolicyController {

    private final CancellationPolicyService policyService;

    @PostMapping
    public ResponseEntity<CancellationPolicyResponseDTO> createPolicy(@Valid @RequestBody CancellationPolicyRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.createPolicy(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CancellationPolicyResponseDTO> getPolicy(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @GetMapping
    public ResponseEntity<List<CancellationPolicyResponseDTO>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<CancellationPolicyResponseDTO>> getByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(policyService.getPoliciesByProperty(propertyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CancellationPolicyResponseDTO> updatePolicy(@PathVariable Long id,
                                                                       @Valid @RequestBody CancellationPolicyRequestDTO dto) {
        return ResponseEntity.ok(policyService.updatePolicy(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
}
