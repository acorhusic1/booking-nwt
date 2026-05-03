package com.bookingnwt.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {
    private UserResponse user;
    private UserPreferenceResponse preference;
    private List<IdentityVerificationResponse> verifications;
}
