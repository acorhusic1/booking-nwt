package com.bookingnwt.notificationservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificationResponseDTO {

    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private Long relatedReservationId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
