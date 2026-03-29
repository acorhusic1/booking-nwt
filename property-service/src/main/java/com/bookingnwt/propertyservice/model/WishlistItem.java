package com.bookingnwt.propertyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_item")
@Getter
@Setter
@NoArgsConstructor
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public WishlistItem(Wishlist wishlist, Long propertyId) {
        this.wishlist = wishlist;
        this.propertyId = propertyId;
        this.addedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) addedAt = LocalDateTime.now();
    }

}
