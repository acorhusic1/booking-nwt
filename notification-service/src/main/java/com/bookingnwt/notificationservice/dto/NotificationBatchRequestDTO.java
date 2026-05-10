package com.bookingnwt.notificationservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Task 4 - Batch unos vise notifikacija u jednom HTTP pozivu.
 */
@Getter
@Setter
@NoArgsConstructor
public class NotificationBatchRequestDTO {

    @NotEmpty(message = "Batch lista ne moze biti prazna")
    @Size(max = 200, message = "Maksimalno 200 notifikacija po batchu")
    @Valid
    private List<NotificationRequestDTO> notifications;
}
