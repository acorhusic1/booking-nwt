package com.bookingnwt.userservice.service.impl;

import com.bookingnwt.userservice.dto.UserRequest;
import com.bookingnwt.userservice.dto.UserResponse;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.mapper.UserMapper;
import com.bookingnwt.userservice.model.User;
import com.bookingnwt.userservice.repository.UserRepository;
import com.bookingnwt.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID " + id + " nije pronađen"));
        return userMapper.toResponse(user);
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

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Korisnik sa ID " + id + " nije pronađen");
        }
        userRepository.deleteById(id);
    }
}
