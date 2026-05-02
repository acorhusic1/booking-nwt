package com.bookingnwt.reservationservice.controller;

import com.bookingnwt.reservationservice.dto.PromoCodeRequestDTO;
import com.bookingnwt.reservationservice.dto.PromoCodeResponseDTO;
import com.bookingnwt.reservationservice.service.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @PostMapping
    public ResponseEntity<PromoCodeResponseDTO> createPromoCode(@Valid @RequestBody PromoCodeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promoCodeService.createPromoCode(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDTO> getPromoCode(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.getPromoCodeById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PromoCodeResponseDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(promoCodeService.getPromoCodeByCode(code));
    }

    @GetMapping
    public ResponseEntity<List<PromoCodeResponseDTO>> getAllPromoCodes() {
        return ResponseEntity.ok(promoCodeService.getAllPromoCodes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromoCodeResponseDTO> updatePromoCode(@PathVariable Long id,
                                                                 @Valid @RequestBody PromoCodeRequestDTO dto) {
        return ResponseEntity.ok(promoCodeService.updatePromoCode(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromoCode(@PathVariable Long id) {
        promoCodeService.deletePromoCode(id);
        return ResponseEntity.noContent().build();
    }
}
