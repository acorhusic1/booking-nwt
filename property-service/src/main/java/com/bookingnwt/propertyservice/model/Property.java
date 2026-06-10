package com.bookingnwt.propertyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "property")
@Getter
@Setter
@NoArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    // Geo koordinate trebaju 6 decimala (~10cm preciznost). Bez ovog Hibernate
    // mapira BigDecimal kao DECIMAL(38,2) → 43.8757 → 43.88 (zaokruzi se).
    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "max_guests")
    private Integer maxGuests;

    // F1/F2 — tip smjestaja (APARTMAN, KUCA, HOTEL, HOSTEL, VILA...) za
    // filtriranje u pretrazi po dokumentaciji. String radi jednostavnosti.
    @Column(name = "property_type", length = 30)
    private String propertyType;

    // F11 — broj pregleda oglasa (statistika za domacina)
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "available")
    private Boolean available = true;

    // F2 — kucna pravila (smoking, pets, parties, children).
    // Cuvamo kao 4 boolean kolone radi jednostavnosti (ne JSON da bi mogli
    // filtrirati u JPA query-ju u buducnosti).
    @Column(name = "rule_no_smoking")
    private Boolean ruleNoSmoking = true;

    @Column(name = "rule_pets_allowed")
    private Boolean rulePetsAllowed = false;

    @Column(name = "rule_parties_allowed")
    private Boolean rulePartiesAllowed = false;

    @Column(name = "rule_children_allowed")
    private Boolean ruleChildrenAllowed = true;

    // F2 — moderacija. Default: PENDING — admin mora odobriti prije nego objekt
    // postane javno vidljiv na /properties listi (filtrira se u getAll/search).
    // nullable=true zbog DDL update-a na postojecu tabelu; PrePersist setuje default.
    @Column(name = "moderation_status", columnDefinition = "VARCHAR(20) DEFAULT 'APPROVED'")
    private String moderationStatus = "PENDING";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyImage> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "property_amenity",
        joinColumns = @JoinColumn(name = "property_id"),
        inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private PricingRule pricingRule;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarBlock> calendarBlocks = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeasonalRule> seasonalRules = new ArrayList<>();

    public Property(Long hostId, String name, String description, String address,
                    String city, String country, BigDecimal latitude, BigDecimal longitude,
                    Integer maxGuests) {
        this.hostId = hostId;
        this.name = name;
        this.description = description;
        this.address = address;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxGuests = maxGuests;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (moderationStatus == null) moderationStatus = "PENDING";
    }

}
