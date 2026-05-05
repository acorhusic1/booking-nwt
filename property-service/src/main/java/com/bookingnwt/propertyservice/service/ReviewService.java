package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.ReviewRequest;
import com.bookingnwt.propertyservice.dto.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse getReviewById(Long id);
    List<ReviewResponse> getReviewsByPropertyId(Long propertyId);
    List<ReviewResponse> getReviewsByGuestId(Long guestId);
    ReviewResponse createReview(ReviewRequest request);
    List<ReviewResponse> createReviews(List<ReviewRequest> requests);
    ReviewResponse addHostReply(Long reviewId, String reply);
}
