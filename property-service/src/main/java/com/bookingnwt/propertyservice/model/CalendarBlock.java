package com.bookingnwt.propertyservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "calendar_block")
@Getter
@Setter
@NoArgsConstructor
public class CalendarBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private String reason;

    @Column(name = "created_by")
    private Long createdBy;

    public CalendarBlock(Property property, LocalDate startDate, LocalDate endDate,
                         String reason, Long createdBy) {
        this.property = property;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.createdBy = createdBy;
    }

}
