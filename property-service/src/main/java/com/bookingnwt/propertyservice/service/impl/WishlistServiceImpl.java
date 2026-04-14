package com.bookingnwt.propertyservice.service.impl;

import com.bookingnwt.propertyservice.dto.WishlistItemRequest;
import com.bookingnwt.propertyservice.dto.WishlistItemResponse;
import com.bookingnwt.propertyservice.dto.WishlistRequest;
import com.bookingnwt.propertyservice.dto.WishlistResponse;
import com.bookingnwt.propertyservice.exception.ResourceNotFoundException;
import com.bookingnwt.propertyservice.mapper.WishlistMapper;
import com.bookingnwt.propertyservice.model.Wishlist;
import com.bookingnwt.propertyservice.model.WishlistItem;
import com.bookingnwt.propertyservice.repository.WishlistItemRepository;
import com.bookingnwt.propertyservice.repository.WishlistRepository;
import com.bookingnwt.propertyservice.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlistsByGuestId(Long guestId) {
        return wishlistRepository.findByGuestId(guestId)
                .stream()
                .map(wishlistMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlistById(Long id) {
        Wishlist wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista želja sa ID " + id + " nije pronađena"));
        return wishlistMapper.toResponse(wishlist);
    }

    @Override
    public WishlistResponse createWishlist(WishlistRequest request) {
        Wishlist wishlist = wishlistMapper.toEntity(request);
        Wishlist saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toResponse(saved);
    }

    @Override
    public void deleteWishlist(Long id) {
        if (!wishlistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lista želja sa ID " + id + " nije pronađena");
        }
        wishlistRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getItemsByWishlistId(Long wishlistId) {
        return wishlistItemRepository.findByWishlistId(wishlistId)
                .stream()
                .map(wishlistMapper::toItemResponse)
                .toList();
    }

    @Override
    public WishlistItemResponse addItem(Long wishlistId, WishlistItemRequest request) {
        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista želja sa ID " + wishlistId + " nije pronađena"));
        WishlistItem item = wishlistMapper.toItemEntity(request);
        item.setWishlist(wishlist);
        WishlistItem saved = wishlistItemRepository.save(item);
        return wishlistMapper.toItemResponse(saved);
    }

    @Override
    public void removeItem(Long itemId) {
        if (!wishlistItemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Stavka liste želja sa ID " + itemId + " nije pronađena");
        }
        wishlistItemRepository.deleteById(itemId);
    }
}
