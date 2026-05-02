package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.ReviewRequest;
import com.bookingnwt.propertyservice.dto.ReviewResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.ReviewMapper;
import com.bookingnwt.propertyservice.model.Review;
import com.bookingnwt.propertyservice.repository.ReviewRepository;
import com.bookingnwt.propertyservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recenzija sa ID " + id + " nije pronađena"));
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByPropertyId(Long propertyId) {
        return reviewRepository.findByPropertyId(propertyId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByGuestId(Long guestId) {
        return reviewRepository.findByGuestId(guestId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Override
    public ReviewResponse createReview(ReviewRequest request) {
        Review review = reviewMapper.toEntity(request);
        BigDecimal overall = request.getRatingCleanliness()
                .add(request.getRatingLocation())
                .add(request.getRatingCommunication())
                .add(request.getRatingValue())
                .add(request.getRatingAccuracy())
                .divide(new BigDecimal("5"), 2, RoundingMode.HALF_UP);
        review.setOverallRating(overall);
        Review saved = reviewRepository.save(review);
        return reviewMapper.toResponse(saved);
    }

    @Override
    public ReviewResponse addHostReply(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Recenzija sa ID " + reviewId + " nije pronađena"));
        review.setHostReply(reply);
        review.setRepliedAt(LocalDateTime.now());
        Review updated = reviewRepository.save(review);
        return reviewMapper.toResponse(updated);
    }
}
