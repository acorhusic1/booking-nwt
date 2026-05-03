package com.bookingnwt.userservice.controller;

import com.bookingnwt.userservice.dto.UserPatchRequest;
import com.bookingnwt.userservice.dto.UserRequest;
import com.bookingnwt.userservice.dto.UserResponse;
import com.bookingnwt.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Non-trivial API #1: Pagination and Sorting.
     * Example: GET /api/users/paginated?page=0&size=10&sort=lastName,asc
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<UserResponse>> getAllUsersPaginated(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsersPaginated(pageable));
    }

    /**
     * Non-trivial API #2: Custom JPQL Query — search by role and/or active status.
     * Example: GET /api/users/search?role=HOST&active=true&page=0&size=5
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(role, active, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Non-trivial API #4: Retrieve user with all nested relationships.
     * Uses EntityGraph to avoid N+1 queries.
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<com.bookingnwt.userservice.dto.UserDetailsResponse> getUserDetailsById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserDetailsById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Non-trivial API #3: PATCH — partial update of user profile.
     * Only provided (non-null) fields are updated.
     * Example: PATCH /api/users/1  body: {"firstName": "NewName"}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patchUser(@PathVariable Long id,
                                                  @Valid @RequestBody UserPatchRequest request) {
        return ResponseEntity.ok(userService.patchUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

