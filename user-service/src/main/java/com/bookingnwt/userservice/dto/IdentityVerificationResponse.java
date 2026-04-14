package com.bookingnwt.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdentityVerificationResponse {
    private Long id;
    private Long userId;
    private String documentType;
    private String documentNumber;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private Long verifiedBy;
}
