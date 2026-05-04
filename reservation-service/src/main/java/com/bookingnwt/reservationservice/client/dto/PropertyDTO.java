package com.bookingnwt.reservationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDTO {
    private Long id;
    private Long hostId;
    private String name;
    private String city;
    private Integer maxGuests;
    private Boolean isActive;
}
