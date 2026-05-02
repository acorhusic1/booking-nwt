package com.bookingnwt.propertyservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemRequest {

    @NotNull(message = "Property ID je obavezan")
    private Long propertyId;
}
