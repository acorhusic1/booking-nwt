package com.bookingnwt.notificationservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Task 4 - PATCH (parcijalno azuriranje notifikacije).
 *
 * Sva polja su opcionalna; ako klijent posalje vrijednost, ona se primjenjuje.
 * Tipicno se koristi za:
 *   - oznacavanje notifikacije kao procitane (isRead=true)
 *   - korekciju naslova/sadrzaja prije nego sto je korisnik vidi
 */
@Getter
@Setter
@NoArgsConstructor
public class NotificationPatchDTO {

    private String title;

    private String content;

    private String type;

    private Boolean isRead;
}
