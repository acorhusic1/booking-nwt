package com.bookingnwt.userservice.service.impl;

import com.bookingnwt.userservice.dto.UserPatchRequest;
import com.bookingnwt.userservice.dto.UserRequest;
import com.bookingnwt.userservice.dto.UserResponse;
import com.bookingnwt.userservice.exception.DuplicateResourceException;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.mapper.UserMapper;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.model.UserRole;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final com.bookingnwt.userservice.mapper.IdentityVerificationMapper verificationMapper;
    private final com.bookingnwt.userservice.mapper.UserPreferenceMapper preferenceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    /**
     * Non-trivial API #1: Pagination and Sorting.
     * Uses Spring Data's Pageable to return a page of users with sorting support.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    /**
     * Non-trivial API #2: Custom JPQL Query.
     * Searches users by optional role and active status filters.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String role, Boolean isActive, Pageable pageable) {
        UserRole userRole = null;
        if (role != null && !role.isBlank()) {
            try {
                userRole = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Nevažeća uloga: " + role + ". Dozvoljene: GUEST, HOST, ADMIN");
            }
        }
        return userRepository.searchByRoleAndStatus(userRole, isActive, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID " + id + " nije pronađen"));
        return userMapper.toResponse(user);
    }

    /**
     * Non-trivial API #4: EntityGraph Optimization.
     * Uses @NamedEntityGraph to fetch User along with IdentityVerification and UserPreference in a single query.
     */
    @Override
    @Transactional(readOnly = true)
    public com.bookingnwt.userservice.dto.UserDetailsResponse getUserDetailsById(Long id) {
        User user = userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID " + id + " nije pronađen"));
        
        return com.bookingnwt.userservice.dto.UserDetailsResponse.builder()
                .user(userMapper.toResponse(user))
                .preference(user.getPreference() != null ? preferenceMapper.toResponse(user.getPreference()) : null)
                .verifications(user.getVerifications() != null ? user.getVerifications().stream().map(verificationMapper::toResponse).toList() : List.of())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa emailom " + email + " nije pronađen"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Korisnik sa emailom " + request.getEmail() + " već postoji");
        }
        User user = userMapper.toEntity(request);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID " + id + " nije pronađen"));
        userMapper.updateEntity(request, user);
        User updated = userRepository.save(user);
        return userMapper.toResponse(updated);
    }

    /**
     * Non-trivial API #3: PATCH — partial update.
     * Only non-null fields in the request are applied to the entity.
     * This avoids overwriting fields the client didn't intend to change.
     */
    @Override
    public UserResponse patchUser(Long id, UserPatchRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID " + id + " nije pronađen"));

        if (request.getEmail() != null) {
            // Check uniqueness only if the email is actually changing
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email " + request.getEmail() + " je već u upotrebi");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPasswordHash(request.getPassword());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getRole() != null) {
            try {
                user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Nevažeća uloga: " + request.getRole());
            }
        }

        User updated = userRepository.save(user);
        return userMapper.toResponse(updated);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Korisnik sa ID " + id + " nije pronađen");
        }
        userRepository.deleteById(id);
    }
}

