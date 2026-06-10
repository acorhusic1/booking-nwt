package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.client.ReservationClient;
import com.bookingnwt.propertyservice.client.UserClient;
import com.bookingnwt.propertyservice.client.dto.ReservationDTO;
import com.bookingnwt.propertyservice.dto.UserDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserClient userClient;
    private final ReservationClient reservationClient;

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recenzija sa ID " + id + " nije pronađena"));
        ReviewResponse response = reviewMapper.toResponse(review);
        enrichWithGuestName(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByPropertyId(Long propertyId) {
        return reviewRepository.findByPropertyId(propertyId)
                .stream()
                .map(reviewMapper::toResponse)
                .peek(this::enrichWithGuestName)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByGuestId(Long guestId) {
        return reviewRepository.findByGuestId(guestId)
                .stream()
                .map(reviewMapper::toResponse)
                .peek(this::enrichWithGuestName)
                .toList();
    }

    private void enrichWithGuestName(ReviewResponse response) {
        try {
            UserDTO user = userClient.getUserById(response.getGuestId());
            response.setGuestName(user.getFirstName() + " " + user.getLastName());
        } catch (Exception e) {
            response.setGuestName("Korisnik Nepoznat");
        }
    }

    @Override
    public ReviewResponse createReview(ReviewRequest request) {
        validateReviewEligibility(request);
        Review review = reviewMapper.toEntity(request);
        calculateOverallRating(review, request);
        Review saved = reviewRepository.save(review);
        return reviewMapper.toResponse(saved);
    }

    @Override
    public List<ReviewResponse> createReviews(List<ReviewRequest> requests) {
        List<Review> reviews = requests.stream()
                .map(request -> {
                    validateReviewEligibility(request);
                    Review review = reviewMapper.toEntity(request);
                    calculateOverallRating(review, request);
                    return review;
                })
                .toList();
        List<Review> saved = reviewRepository.saveAll(reviews);
        return saved.stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    /**
     * K9 (F7) — server-side validacija: "samo gosti sa završenom rezervacijom
     * mogu ocjenjivati". Do sada je gating bio samo u UI-ju pa je svaki GUEST
     * mogao POST-ati recenziju za bilo koji objekat, više puta.
     *
     * Provjere: rezervacija postoji + pripada gostu i property-ju iz request-a
     * + boravak je završen (COMPLETED, ili checkout prošao za CONFIRMED/ACTIVE
     * ako scheduler još nije odradio tranziciju) + nema duple recenzije.
     * Fail-closed: ako reservation-service nije dostupan, recenzija se odbija.
     */
    private void validateReviewEligibility(ReviewRequest request) {
        if (reviewRepository.existsByReservationId(request.getReservationId())) {
            throw new IllegalArgumentException(
                    "Recenzija za rezervaciju " + request.getReservationId() + " već postoji");
        }

        ReservationDTO reservation;
        try {
            reservation = reservationClient.getReservation(request.getReservationId());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Trenutno ne možemo provjeriti rezervaciju — pokušajte ponovo za nekoliko minuta");
        }
        if (reservation == null) {
            throw new ResourceNotFoundException(
                    "Rezervacija " + request.getReservationId() + " nije pronađena");
        }
        if (!request.getGuestId().equals(reservation.getGuestId())
                || !request.getPropertyId().equals(reservation.getPropertyId())) {
            throw new IllegalArgumentException(
                    "Recenzija se ne poklapa sa rezervacijom (gost/smještaj)");
        }

        String status = reservation.getStatus() != null ? reservation.getStatus().toUpperCase() : "";
        boolean checkoutPassed = reservation.getCheckOut() != null
                && reservation.getCheckOut().isBefore(LocalDate.now().plusDays(1));
        boolean stayFinished = "COMPLETED".equals(status)
                || (checkoutPassed && ("CONFIRMED".equals(status) || "ACTIVE".equals(status)));
        if (!stayFinished) {
            throw new IllegalArgumentException(
                    "Recenziju možete ostaviti tek nakon završenog boravka");
        }
    }

    private void calculateOverallRating(Review review, ReviewRequest request) {
        BigDecimal overall = request.getRatingCleanliness()
                .add(request.getRatingLocation())
                .add(request.getRatingCommunication())
                .add(request.getRatingValue())
                .add(request.getRatingAccuracy())
                .divide(new BigDecimal("5"), 2, RoundingMode.HALF_UP);
        review.setOverallRating(overall);
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
