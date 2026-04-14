package com.bookingnwt.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationRequestDTO {

    @NotNull(message = "ID korisnika je obavezan")
    private Long userId;

    @NotBlank(message = "Tip notifikacije je obavezan")
    private String type;

    @NotBlank(message = "Naslov je obavezan")
    private String title;

    private String content;

    private Long relatedReservationId;
}
