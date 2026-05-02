package com.bookingnwt.propertyservice.repository;

import com.bookingnwt.propertyservice.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByGuestId(Long guestId);
}
