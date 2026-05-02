package com.bookingnwt.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageRequestDTO {

    @NotNull(message = "ID konverzacije je obavezan")
    private Long conversationId;

    @NotNull(message = "ID pošiljaoca je obavezan")
    private Long senderId;

    @NotBlank(message = "Sadržaj poruke je obavezan")
    private String content;
}
