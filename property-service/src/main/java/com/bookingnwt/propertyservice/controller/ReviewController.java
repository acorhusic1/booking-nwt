package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.ReviewRequest;
import com.bookingnwt.propertyservice.dto.ReviewResponse;
import com.bookingnwt.propertyservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByPropertyId(@PathVariable Long propertyId) {
        return ResponseEntity.ok(reviewService.getReviewsByPropertyId(propertyId));
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(reviewService.getReviewsByGuestId(guestId));
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GUEST')")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse created = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/batch")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<List<ReviewResponse>> createReviews(@Valid @RequestBody List<ReviewRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReviews(requests));
    }

    @PostMapping("/batch")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<List<ReviewResponse>> createReviews(@Valid @RequestBody List<ReviewRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReviews(requests));
    }

    @PutMapping("/{id}/reply")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('HOST')")
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<ReviewResponse> addHostReply(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        return ResponseEntity.ok(reviewService.addHostReply(id, reply));
    }
}
