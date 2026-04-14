package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByWishlistId(Long wishlistId);
}
