package com.bookingnwt.propertyservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishlistRequest {

    @NotNull(message = "Guest ID je obavezan")
    private Long guestId;

    @NotBlank(message = "Naziv liste je obavezan")
    private String name;
}
