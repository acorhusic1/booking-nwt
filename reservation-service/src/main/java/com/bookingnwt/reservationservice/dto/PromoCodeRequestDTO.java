package com.bookingnwt.reservationservice.dto;

import com.bookingnwt.reservationservice.model.DiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeRequestDTO {

    @NotBlank(message = "Kod je obavezan")
    private String code;

    private String description;

    @NotNull(message = "Tip popusta je obavezan")
    private DiscountType discountType;

    @NotNull(message = "Vrijednost popusta je obavezna")
    @Min(value = 0, message = "Popust ne može biti negativan")
    private BigDecimal discountValue;

    private Integer minNights;

    @NotNull(message = "Datum početka je obavezan")
    private LocalDate validFrom;

    @NotNull(message = "Datum isteka je obavezan")
    private LocalDate validTo;

    private Integer maxUses;

    private Long createdBy;
}
