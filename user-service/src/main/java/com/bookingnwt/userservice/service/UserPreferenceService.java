package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.UserPreferenceRequest;
import com.bookingnwt.userservice.dto.UserPreferenceResponse;

public interface UserPreferenceService {
    UserPreferenceResponse getPreferenceByUserId(Long userId);
    UserPreferenceResponse createOrUpdatePreference(UserPreferenceRequest request);
    void deletePreference(Long userId);
}
