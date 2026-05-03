package com.bookingnwt.userservice.service;

import com.bookingnwt.userservice.dto.UserPatchRequest;
import com.bookingnwt.userservice.dto.UserRequest;
import com.bookingnwt.userservice.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    Page<UserResponse> getAllUsersPaginated(Pageable pageable);
    Page<UserResponse> searchUsers(String role, Boolean isActive, Pageable pageable);
    UserResponse getUserById(Long id);
    com.bookingnwt.userservice.dto.UserDetailsResponse getUserDetailsById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(Long id, UserRequest request);
    UserResponse patchUser(Long id, UserPatchRequest request);
    void deleteUser(Long id);
}
