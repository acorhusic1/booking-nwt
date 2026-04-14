package com.bookingnwt.notificationservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MessageResponseDTO {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}
