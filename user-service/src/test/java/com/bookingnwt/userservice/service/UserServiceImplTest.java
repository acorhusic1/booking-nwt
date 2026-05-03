package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.UserPatchRequest;
import com.bookingnwt.userservice.dto.UserRequest;
import com.bookingnwt.userservice.dto.UserResponse;
import com.bookingnwt.userservice.exception.DuplicateResourceException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Mock
    private com.bookingnwt.userservice.mapper.IdentityVerificationMapper verificationMapper;

    @Mock
    private com.bookingnwt.userservice.mapper.UserPreferenceMapper preferenceMapper;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequest userRequest;
    private UserResponse userResponse;
    private com.bookingnwt.userservice.dto.UserDetailsResponse userDetailsResponse;

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

    // ==================== Existing tests ====================

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
        when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
        when(userMapper.toEntity(userRequest)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pass");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(userRequest);

        assertThat(result.getEmail()).isEqualTo("test@email.com");
        verify(userRepository).save(user);
    }

    @Test
    void createUser_shouldThrowDuplicate_whenEmailExists() {
        when(userRepository.existsByEmail("test@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("test@email.com");
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

    // ==================== NEW: Pagination tests ====================

    @Test
    void getAllUsersPaginated_shouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.getAllUsersPaginated(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@email.com");
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getAllUsersPaginated_shouldReturnEmptyPage_whenNoUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<UserResponse> result = userService.getAllUsersPaginated(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ==================== NEW: Custom JPQL search tests ====================

    @Test
    void searchUsers_shouldReturnFilteredUsers_byRole() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.searchByRoleAndStatus(UserRole.GUEST, null, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.searchUsers("GUEST", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRole()).isEqualTo("GUEST");
    }

    @Test
    void searchUsers_shouldReturnFilteredUsers_byRoleAndActive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.searchByRoleAndStatus(UserRole.HOST, true, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.searchUsers("HOST", true, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchUsers_shouldReturnAllUsers_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.searchByRoleAndStatus(null, null, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.searchUsers(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchUsers_shouldThrowException_whenInvalidRole() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> userService.searchUsers("INVALID_ROLE", null, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nevažeća uloga");
    }

    // ==================== NEW: PATCH tests ====================

    @Test
    void patchUser_shouldUpdateOnlyFirstName() {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .firstName("NovoIme")
                .build();

        UserResponse patchedResponse = new UserResponse();
        patchedResponse.setId(1L);
        patchedResponse.setEmail("test@email.com");
        patchedResponse.setFirstName("NovoIme");
        patchedResponse.setLastName("Ivić");
        patchedResponse.setRole("GUEST");
        patchedResponse.setIsActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(patchedResponse);

        UserResponse result = userService.patchUser(1L, patchRequest);

        assertThat(result.getFirstName()).isEqualTo("NovoIme");
        // Verify only firstName was set on the entity
        assertThat(user.getFirstName()).isEqualTo("NovoIme");
        // Other fields should remain unchanged
        assertThat(user.getLastName()).isEqualTo("Ivić");
        assertThat(user.getEmail()).isEqualTo("test@email.com");
        verify(userRepository).save(user);
    }

    @Test
    void patchUser_shouldUpdateEmail_whenNewEmailIsUnique() {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .email("new@email.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.patchUser(1L, patchRequest);

        assertThat(user.getEmail()).isEqualTo("new@email.com");
        verify(userRepository).save(user);
    }

    @Test
    void patchUser_shouldThrowDuplicate_whenEmailAlreadyExists() {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .email("taken@email.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.patchUser(1L, patchRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("taken@email.com");
    }

    @Test
    void patchUser_shouldNotCheckDuplicate_whenEmailUnchanged() {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .email("test@email.com") // same as current email
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.patchUser(1L, patchRequest);

        // existsByEmail should NOT be called since email didn't change
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void patchUser_shouldThrowNotFound_whenUserDoesNotExist() {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .firstName("NovoIme")
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.patchUser(99L, patchRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void patchUser_shouldThrowException_whenInvalidRole() {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .role("INVALID")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.patchUser(1L, patchRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nevažeća uloga");
    }

    @Test
    void patchUser_shouldUpdateMultipleFields() {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .firstName("Ahmed")
                .lastName("Novo")
                .phone("+38762000000")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.patchUser(1L, patchRequest);

        assertThat(user.getFirstName()).isEqualTo("Ahmed");
        assertThat(user.getLastName()).isEqualTo("Novo");
        assertThat(user.getPhone()).isEqualTo("+38762000000");
        // email & role should remain unchanged
        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getRole()).isEqualTo(UserRole.GUEST);
    }

    @Test
    void patchUser_shouldUpdatePassword_whenProvided() {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .password("newPassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new_hashed_pass");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.patchUser(1L, patchRequest);

        assertThat(user.getPasswordHash()).isEqualTo("new_hashed_pass");
        verify(passwordEncoder).encode("newPassword123");
    }
    @Test
    void getUserDetailsById_shouldReturnUserDetails() {
        when(userRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        com.bookingnwt.userservice.dto.UserDetailsResponse result = userService.getUserDetailsById(1L);

        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getEmail()).isEqualTo("test@email.com");
    }
}
