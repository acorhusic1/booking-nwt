package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.ReviewRequest;
import com.bookingnwt.propertyservice.dto.ReviewResponse;
import com.bookingnwt.propertyservice.exception.GlobalExceptionHandler;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import(GlobalExceptionHandler.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    private ReviewResponse createReviewResponse() {
        ReviewResponse r = new ReviewResponse();
        r.setId(1L);
        r.setReservationId(100L);
        r.setGuestId(1L);
        r.setPropertyId(1L);
        r.setHostId(1L);
        r.setRatingCleanliness(new BigDecimal("4.5"));
        r.setRatingLocation(new BigDecimal("4.0"));
        r.setRatingCommunication(new BigDecimal("5.0"));
        r.setRatingValue(new BigDecimal("4.5"));
        r.setRatingAccuracy(new BigDecimal("4.0"));
        r.setOverallRating(new BigDecimal("4.40"));
        r.setComment("Odličan smještaj!");
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    @Test
    void createReviews_shouldReturn201() throws Exception {
        ReviewRequest request = new ReviewRequest();
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

        when(reviewService.createReviews(any())).thenReturn(List.of(createReviewResponse()));

        mockMvc.perform(post("/api/reviews/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].comment").value("Odličan smještaj!"));
    }

    @Test
    void getReviewById_shouldReturn200() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(createReviewResponse());

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Odličan smještaj!"));
    }

    @Test
    void getReviewById_shouldReturn404_whenNotFound() throws Exception {
        when(reviewService.getReviewById(99L)).thenThrow(new ResourceNotFoundException("Nije pronađena"));

        mockMvc.perform(get("/api/reviews/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReviewsByPropertyId_shouldReturn200() throws Exception {
        when(reviewService.getReviewsByPropertyId(1L)).thenReturn(List.of(createReviewResponse()));

        mockMvc.perform(get("/api/reviews/property/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].propertyId").value(1));
    }

    @Test
    void getReviewsByGuestId_shouldReturn200() throws Exception {
        when(reviewService.getReviewsByGuestId(1L)).thenReturn(List.of(createReviewResponse()));

        mockMvc.perform(get("/api/reviews/guest/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].guestId").value(1));
    }

    @Test
    void createReview_shouldReturn201_whenValid() throws Exception {
        ReviewRequest request = new ReviewRequest();
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

        when(reviewService.createReview(any())).thenReturn(createReviewResponse());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.overallRating").value(4.40));
    }

    @Test
    void createReview_shouldReturn400_whenInvalid() throws Exception {
        ReviewRequest request = new ReviewRequest();
        // Missing all required fields

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addHostReply_shouldReturn200() throws Exception {
        ReviewResponse replyResponse = createReviewResponse();
        replyResponse.setHostReply("Hvala vam!");
        when(reviewService.addHostReply(eq(1L), eq("Hvala vam!"))).thenReturn(replyResponse);

        mockMvc.perform(put("/api/reviews/1/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reply\": \"Hvala vam!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hostReply").value("Hvala vam!"));
    }
}
