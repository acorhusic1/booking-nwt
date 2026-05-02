package com.bookingnwt.reservationservice.dto;

import com.bookingnwt.reservationservice.model.ProblemReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemReportResponseDTO {

    private Long id;
    private Long reservationId;
    private Long reporterId;
    private String category;
    private String description;
    private ProblemReportStatus status;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
}
