package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.UserRequest;
import com.bookingnwt.userservice.dto.UserResponse;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.mapper.UserMapper;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.model.UserRole;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = new User("test@email.com", "hash123", "Ivo", "Ivić", "+38761111111", UserRole.GUEST);
        user.setId(1L);

        userRequest = new UserRequest("test@email.com", "pass123", "Ivo", "Ivić", "+38761111111", "GUEST");

        userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setEmail("test@email.com");
        userResponse.setFirstName("Ivo");
        userResponse.setLastName("Ivić");
        userResponse.setRole("GUEST");
        userResponse.setIsActive(true);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@email.com");
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_shouldReturnUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@email.com");
    }

    @Test
    void getUserById_shouldThrowException_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenExists() {
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserByEmail("test@email.com");

        assertThat(result.getEmail()).isEqualTo("test@email.com");
    }

    @Test
    void getUserByEmail_shouldThrowException_whenNotFound() {
        when(userRepository.findByEmail("nema@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("nema@email.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nema@email.com");
    }

    @Test
    void createUser_shouldSaveAndReturnUser() {
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(userRequest);

        assertThat(result.getEmail()).isEqualTo("test@email.com");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateEntity(userRequest, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, userRequest);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userMapper).updateEntity(userRequest, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrowException_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, userRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_shouldDelete_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowException_whenNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
