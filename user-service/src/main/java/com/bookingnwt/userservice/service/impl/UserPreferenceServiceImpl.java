package com.bookingnwt.userservice.service.impl;

import com.bookingnwt.userservice.dto.UserPreferenceRequest;
import com.bookingnwt.userservice.dto.UserPreferenceResponse;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.mapper.UserPreferenceMapper;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.model.UserPreference;
import com.bookingnwt.userservice.repository.UserPreferenceRepository;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final UserPreferenceMapper preferenceMapper;

    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getPreferenceByUserId(Long userId) {
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Preferencije za korisnika sa ID " + userId + " nisu pronađene"));
        return preferenceMapper.toResponse(preference);
    }

    @Override
    public UserPreferenceResponse createOrUpdatePreference(UserPreferenceRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Korisnik sa ID " + request.getUserId() + " nije pronađen"));

        Optional<UserPreference> existing = preferenceRepository.findByUserId(request.getUserId());

        UserPreference preference;
        if (existing.isPresent()) {
            preference = existing.get();
            preferenceMapper.updateEntity(request, preference);
        } else {
            preference = preferenceMapper.toEntity(request);
            preference.setUser(user);
        }

        UserPreference saved = preferenceRepository.save(preference);
        return preferenceMapper.toResponse(saved);
    }

    @Override
    public void deletePreference(Long userId) {
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Preferencije za korisnika sa ID " + userId + " nisu pronađene"));
        preferenceRepository.delete(preference);
    }
}
