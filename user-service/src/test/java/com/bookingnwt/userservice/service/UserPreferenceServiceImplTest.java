package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.UserPreferenceRequest;
import com.bookingnwt.userservice.dto.UserPreferenceResponse;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.mapper.UserPreferenceMapper;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.model.UserPreference;
import com.bookingnwt.userservice.model.UserRole;
import com.bookingnwt.userservice.repository.UserPreferenceRepository;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.service.impl.UserPreferenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceImplTest {

    @Mock
    private UserPreferenceRepository preferenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceMapper preferenceMapper;

    @InjectMocks
    private UserPreferenceServiceImpl preferenceService;

    private User user;
    private UserPreference preference;
    private UserPreferenceRequest request;
    private UserPreferenceResponse response;

    @BeforeEach
    void setUp() {
        user = new User("test@email.com", "hash123", "Ivo", "Ivić", "+38761111111", UserRole.GUEST);
        user.setId(1L);

        preference = new UserPreference(user, "bs", "APARTMENT", new BigDecimal("50"), new BigDecimal("200"));
        preference.setId(1L);

        request = new UserPreferenceRequest(1L, "bs", "APARTMENT", new BigDecimal("50"), new BigDecimal("200"));

        response = new UserPreferenceResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setPreferredLanguage("bs");
        response.setPropertyType("APARTMENT");
        response.setMinPrice(new BigDecimal("50"));
        response.setMaxPrice(new BigDecimal("200"));
    }

    @Test
    void getPreferenceByUserId_shouldReturnPreference_whenExists() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        when(preferenceMapper.toResponse(preference)).thenReturn(response);

        UserPreferenceResponse result = preferenceService.getPreferenceByUserId(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getPreferredLanguage()).isEqualTo("bs");
    }

    @Test
    void getPreferenceByUserId_shouldThrowException_whenNotFound() {
        when(preferenceRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preferenceService.getPreferenceByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createOrUpdatePreference_shouldCreateNew_whenNotExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(preferenceMapper.toEntity(request)).thenReturn(preference);
        when(preferenceRepository.save(preference)).thenReturn(preference);
        when(preferenceMapper.toResponse(preference)).thenReturn(response);

        UserPreferenceResponse result = preferenceService.createOrUpdatePreference(request);

        assertThat(result.getPreferredLanguage()).isEqualTo("bs");
        verify(preferenceMapper).toEntity(request);
    }

    @Test
    void createOrUpdatePreference_shouldUpdate_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));
        doNothing().when(preferenceMapper).updateEntity(request, preference);
        when(preferenceRepository.save(preference)).thenReturn(preference);
        when(preferenceMapper.toResponse(preference)).thenReturn(response);

        UserPreferenceResponse result = preferenceService.createOrUpdatePreference(request);

        assertThat(result.getUserId()).isEqualTo(1L);
        verify(preferenceMapper).updateEntity(request, preference);
    }

    @Test
    void createOrUpdatePreference_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        request.setUserId(99L);

        assertThatThrownBy(() -> preferenceService.createOrUpdatePreference(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletePreference_shouldDelete_whenExists() {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(preference));

        preferenceService.deletePreference(1L);

        verify(preferenceRepository).delete(preference);
    }

    @Test
    void deletePreference_shouldThrow_whenNotFound() {
        when(preferenceRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preferenceService.deletePreference(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
