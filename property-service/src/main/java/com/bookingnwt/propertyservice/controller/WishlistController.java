package com.bookingnwt.propertyservice.controller;

import com.bookingnwt.propertyservice.dto.WishlistItemRequest;
import com.bookingnwt.propertyservice.dto.WishlistItemResponse;
import com.bookingnwt.propertyservice.dto.WishlistRequest;
import com.bookingnwt.propertyservice.dto.WishlistResponse;
import com.bookingnwt.propertyservice.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<WishlistResponse>> getWishlistsByGuestId(@PathVariable Long guestId) {
        return ResponseEntity.ok(wishlistService.getWishlistsByGuestId(guestId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WishlistResponse> getWishlistById(@PathVariable Long id) {
        return ResponseEntity.ok(wishlistService.getWishlistById(id));
    }

    @PostMapping
    public ResponseEntity<WishlistResponse> createWishlist(@Valid @RequestBody WishlistRequest request) {
        WishlistResponse created = wishlistService.createWishlist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWishlist(@PathVariable Long id) {
        wishlistService.deleteWishlist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{wishlistId}/items")
    public ResponseEntity<List<WishlistItemResponse>> getItems(@PathVariable Long wishlistId) {
        return ResponseEntity.ok(wishlistService.getItemsByWishlistId(wishlistId));
    }

    @PostMapping("/{wishlistId}/items")
    public ResponseEntity<WishlistItemResponse> addItem(@PathVariable Long wishlistId,
                                                        @Valid @RequestBody WishlistItemRequest request) {
        WishlistItemResponse created = wishlistService.addItem(wishlistId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{wishlistId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long wishlistId,
                                           @PathVariable Long itemId) {
        wishlistService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
