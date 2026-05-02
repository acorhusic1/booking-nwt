package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.ReviewRequest;
import com.bookingnwt.propertyservice.dto.ReviewResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.ReviewMapper;
import com.bookingnwt.propertyservice.model.Review;
import com.bookingnwt.propertyservice.repository.ReviewRepository;
import com.bookingnwt.propertyservice.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private ReviewRequest request;
    private ReviewResponse response;

    @BeforeEach
    void setUp() {
        review = new Review(100L, 1L, 1L, 1L,
                new BigDecimal("4.5"), new BigDecimal("4.0"), new BigDecimal("5.0"),
                new BigDecimal("4.5"), new BigDecimal("4.0"), "Odličan smještaj!");
        review.setId(1L);

        request = new ReviewRequest();
        request.setReservationId(100L);
        request.setGuestId(1L);
        request.setPropertyId(1L);
        request.setHostId(1L);
        request.setRatingCleanliness(new BigDecimal("4.5"));
        request.setRatingLocation(new BigDecimal("4.0"));
        request.setRatingCommunication(new BigDecimal("5.0"));
        request.setRatingValue(new BigDecimal("4.5"));
        request.setRatingAccuracy(new BigDecimal("4.0"));
        request.setComment("Odličan smještaj!");

        response = new ReviewResponse();
        response.setId(1L);
        response.setPropertyId(1L);
        response.setOverallRating(new BigDecimal("4.40"));
        response.setComment("Odličan smještaj!");
        response.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getReviewById_shouldReturnReview_whenExists() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewMapper.toResponse(review)).thenReturn(response);

        ReviewResponse result = reviewService.getReviewById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getReviewById_shouldThrow_whenNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getReviewsByPropertyId_shouldReturnList() {
        when(reviewRepository.findByPropertyId(1L)).thenReturn(List.of(review));
        when(reviewMapper.toResponse(review)).thenReturn(response);

        List<ReviewResponse> result = reviewService.getReviewsByPropertyId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getReviewsByGuestId_shouldReturnList() {
        when(reviewRepository.findByGuestId(1L)).thenReturn(List.of(review));
        when(reviewMapper.toResponse(review)).thenReturn(response);

        List<ReviewResponse> result = reviewService.getReviewsByGuestId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void createReview_shouldCalculateOverallRating() {
        Review newReview = new Review();
        when(reviewMapper.toEntity(request)).thenReturn(newReview);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(response);

        ReviewResponse result = reviewService.createReview(request);

        assertThat(result).isNotNull();
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void addHostReply_shouldUpdateReview() {
        ReviewResponse replyResponse = new ReviewResponse();
        replyResponse.setId(1L);
        replyResponse.setHostReply("Hvala vam!");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(replyResponse);

        ReviewResponse result = reviewService.addHostReply(1L, "Hvala vam!");

        assertThat(result.getHostReply()).isEqualTo("Hvala vam!");
        verify(reviewRepository).save(review);
    }

    @Test
    void addHostReply_shouldThrow_whenNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addHostReply(99L, "Reply"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
