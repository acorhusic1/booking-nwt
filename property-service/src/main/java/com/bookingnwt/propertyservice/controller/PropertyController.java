package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.client.UserClient;
import com.bookingnwt.propertyservice.dto.PropertyPatchRequest;
import com.bookingnwt.propertyservice.dto.PropertyRequest;
import com.bookingnwt.propertyservice.dto.PropertyResponse;
import com.bookingnwt.propertyservice.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;
    private final UserClient userClient;

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

    /**
     * F18 — dinamicko ucitavanje za mapu: vraca smjestaje unutar vidljivog
     * bounding box-a. Frontend poziva na svako pomicanje/zoom mape.
     */
    @GetMapping("/in-bounds")
    public ResponseEntity<List<PropertyResponse>> getPropertiesInBounds(
            @RequestParam java.math.BigDecimal minLat,
            @RequestParam java.math.BigDecimal maxLat,
            @RequestParam java.math.BigDecimal minLng,
            @RequestParam java.math.BigDecimal maxLng) {
        return ResponseEntity.ok(propertyService.getPropertiesInBounds(minLat, maxLat, minLng, maxLng));
    }

    /**
     * F11 — registruje pregled oglasa (frontend zove pri otvaranju stranice
     * detalja). Public — i neulogovani posjetioci se broje.
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> registerView(@PathVariable Long id) {
        propertyService.registerView(id);
        return ResponseEntity.noContent().build();
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

    // F2 — Admin moderacija: APPROVE / REJECT objekat. Tek nakon APPROVED objekat
    // postaje vidljiv na public listingu (filter u PropertyServiceImpl.getAllProperties).
    @PutMapping("/{id}/moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> moderateProperty(@PathVariable Long id,
                                                              @RequestParam String status) {
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status) && !"PENDING".equals(status)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(propertyService.updateModerationStatus(id, status));
    }

    @GetMapping("/pending-moderation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PropertyResponse>> getPendingModeration() {
        return ResponseEntity.ok(propertyService.getByModerationStatus("PENDING"));
    }

    @PostMapping
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<?> createProperty(@Valid @RequestBody PropertyRequest request,
                                            jakarta.servlet.http.HttpServletRequest httpRequest) {
        // K8 — HOST objavljuje u SVOJE ime: uid iz tokena pregazi hostId iz body-a
        // (sprjecava objavu "u ime" tudjeg/verifikovanog hosta). ADMIN smije
        // postaviti proizvoljan hostId (npr. seed/migracija).
        Object uidAttr = httpRequest.getAttribute("authUserId");
        if (uidAttr instanceof Long uid && !httpRequest.isUserInRole("ADMIN")) {
            request.setHostId(uid);
        }
        if (request.getHostId() == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error", "HostIdRequired", "message", "hostId je obavezan"));
        }
        // F16 enforce — host MORA imati APPROVED verifikaciju prije nego sto
        // objavi novi objekat. JWT se propagira kroz FeignAuthInterceptor pa
        // user-service moze odgovoriti.
        boolean approved = false;
        try {
            List<java.util.Map<String, Object>> verifs = userClient.getVerifications(request.getHostId());
            approved = verifs != null && verifs.stream()
                    .anyMatch(v -> "APPROVED".equalsIgnoreCase(String.valueOf(v.get("status"))));
        } catch (Exception e) {
            log.error("Verification check failed for hostId={}: {}", request.getHostId(), e.getMessage());
            // Fail-closed — bolje blokirati nego dozvoliti neverifikovan
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                    java.util.Map.of("error", "VerificationCheckFailed",
                            "message", "Nije moguće provjeriti status verifikacije. Pokušajte ponovo za nekoliko trenutaka."));
        }
        if (!approved) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    java.util.Map.of(
                            "error", "VerificationRequired",
                            "message", "Vaš identitet nije verifikovan. Pošaljite zahtjev za verifikaciju i sačekajte odobrenje administratora prije nego što objavite smještaj."
                    )
            );
        }
        PropertyResponse created = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // K8 — HOST smije mijenjati/brisati samo SVOJE objekte (ADMIN sve)
    private void enforceOwnership(Long propertyId, jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (httpRequest.isUserInRole("ADMIN")) return;
        Object uidAttr = httpRequest.getAttribute("authUserId");
        if (uidAttr instanceof Long uid) {
            PropertyResponse property = propertyService.getPropertyById(propertyId);
            if (!uid.equals(property.getHostId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Nemate pravo mijenjati tuđi smještaj");
            }
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> updateProperty(@PathVariable Long id,
                                                           @Valid @RequestBody PropertyRequest request,
                                                           jakarta.servlet.http.HttpServletRequest httpRequest) {
        enforceOwnership(id, httpRequest);
        return ResponseEntity.ok(propertyService.updateProperty(id, request));
    }

    /**
     * PATCH — parcijalni update nekretnine. Samo poslana (non-null) polja se ažuriraju.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> patchProperty(@PathVariable Long id,
                                                          @RequestBody PropertyPatchRequest request,
                                                          jakarta.servlet.http.HttpServletRequest httpRequest) {
        enforceOwnership(id, httpRequest);
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
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id,
                                               jakarta.servlet.http.HttpServletRequest httpRequest) {
        enforceOwnership(id, httpRequest);
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }
}
