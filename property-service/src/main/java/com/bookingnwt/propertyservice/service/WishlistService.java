package com.bookingnwt.propertyservice.service;

import com.bookingnwt.propertyservice.dto.WishlistRequest;
import com.bookingnwt.propertyservice.dto.WishlistResponse;
import com.bookingnwt.propertyservice.dto.WishlistItemRequest;
import com.bookingnwt.propertyservice.dto.WishlistItemResponse;

import java.util.List;

public interface WishlistService {
    List<WishlistResponse> getWishlistsByGuestId(Long guestId);
    WishlistResponse getWishlistById(Long id);
    WishlistResponse createWishlist(WishlistRequest request);
    void deleteWishlist(Long id);
    List<WishlistItemResponse> getItemsByWishlistId(Long wishlistId);
    WishlistItemResponse addItem(Long wishlistId, WishlistItemRequest request);
    void removeItem(Long itemId);
}
