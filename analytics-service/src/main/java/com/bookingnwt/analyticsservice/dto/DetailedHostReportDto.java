package com.bookingnwt.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedHostReportDto {
    private UserDto hostDetails;
    private List<RevenueReportResponseDTO> reports;
}
