package com.bookingnwt.notificationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConversationRequestDTO {

    @NotNull(message = "ID gosta je obavezan")
    private Long guestId;

    @NotNull(message = "ID domaćina je obavezan")
    private Long hostId;

    private Long propertyId;

    private Long reservationId;
}
