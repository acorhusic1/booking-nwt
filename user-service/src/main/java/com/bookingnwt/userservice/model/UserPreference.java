package com.bookingnwt.userservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preference")
@Getter
@Setter
@NoArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "property_type")
    private String propertyType;

    @Column(name = "min_price")
    private BigDecimal minPrice;

    @Column(name = "max_price")
    private BigDecimal maxPrice;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserPreference(User user, String preferredLanguage, String propertyType,
                          BigDecimal minPrice, BigDecimal maxPrice) {
        this.user = user;
        this.preferredLanguage = preferredLanguage;
        this.propertyType = propertyType;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
