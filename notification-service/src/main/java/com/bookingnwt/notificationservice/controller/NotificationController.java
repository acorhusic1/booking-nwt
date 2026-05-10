package com.bookingnwt.notificationservice.controller;

import com.bookingnwt.notificationservice.dto.NotificationBatchRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationPatchDTO;
import com.bookingnwt.notificationservice.dto.NotificationRequestDTO;
import com.bookingnwt.notificationservice.dto.NotificationResponseDTO;
import com.bookingnwt.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'HOST', 'ADMIN')")
    public ResponseEntity<NotificationResponseDTO> createNotification(@Valid @RequestBody NotificationRequestDTO dto) {
        return new ResponseEntity<>(notificationService.createNotification(dto), HttpStatus.CREATED);
    }

    /**
     * Task 4 - Batch insert. Klijent salje listu notifikacija u jednom POST pozivu.
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponseDTO>> createBatch(@Valid @RequestBody NotificationBatchRequestDTO batch) {
        return new ResponseEntity<>(notificationService.createBatch(batch), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    /**
     * Task 4 - Pagination & Sorting na svim notifikacijama.
     * Primjer: GET /api/notifications?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAllNotifications(pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationsByUserId(
            @PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId, pageable));
    }

    /**
     * Task 4 - Custom JPQL query (filtriranje po userId/type/isRead/datumski raspon + paginacija).
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationResponseDTO>> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.search(userId, type, isRead, from, to, pageable));
    }

    @GetMapping("/user/{userId}/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countUnreadByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countUnreadByUserId(userId));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /**
     * Task 4 - PATCH (application/merge-patch+json u praksi se moze koristiti istim payloadom).
     * Sva polja su opcionalna; saljemo samo ono sto se mijenja.
     */
    @PatchMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON_VALUE, "application/merge-patch+json"})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponseDTO> patchNotification(
            @PathVariable Long id, @RequestBody NotificationPatchDTO patch) {
        return ResponseEntity.ok(notificationService.patchNotification(id, patch));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
