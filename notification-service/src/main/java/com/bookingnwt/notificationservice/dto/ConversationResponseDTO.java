package com.bookingnwt.notificationservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ConversationResponseDTO {

    private Long id;
    private Long guestId;
    private Long hostId;
    private Long propertyId;
    private Long reservationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
