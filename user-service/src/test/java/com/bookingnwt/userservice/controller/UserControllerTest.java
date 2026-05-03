package com.bookingnwt.userservice.controller;

import com.bookingnwt.userservice.dto.UserPatchRequest;
import com.bookingnwt.userservice.dto.UserRequest;
import com.bookingnwt.userservice.dto.UserResponse;
import com.bookingnwt.userservice.exception.DuplicateResourceException;
import com.bookingnwt.userservice.exception.GlobalExceptionHandler;
import com.bookingnwt.userservice.exception.ResourceNotFoundException;
import com.bookingnwt.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponse createUserResponse() {
        UserResponse r = new UserResponse();
        r.setId(1L);
        r.setEmail("test@email.com");
        r.setFirstName("Ivo");
        r.setLastName("Ivić");
        r.setRole("GUEST");
        r.setIsActive(true);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    // ==================== Existing tests ====================

    @Test
    void getAllUsers_shouldReturn200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(createUserResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@email.com"));
    }

    @Test
    void getUserById_shouldReturn200_whenExists() throws Exception {
        when(userService.getUserById(1L)).thenReturn(createUserResponse());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ivo"));
    }

    @Test
    void getUserById_shouldReturn404_whenNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("Korisnik sa ID 99 nije pronađen"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createUser_shouldReturn201_whenValid() throws Exception {
        UserRequest request = new UserRequest("test@email.com", "pass123", "Ivo", "Ivić", "+38761111111", "GUEST");
        when(userService.createUser(any(UserRequest.class))).thenReturn(createUserResponse());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void createUser_shouldReturn400_whenInvalid() throws Exception {
        UserRequest request = new UserRequest("", "", "", "", "", null);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void createUser_shouldReturn409_whenDuplicateEmail() throws Exception {
        UserRequest request = new UserRequest("taken@email.com", "pass123", "Ivo", "Ivić", "+38761111111", "GUEST");
        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new DuplicateResourceException("Korisnik sa emailom taken@email.com već postoji"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void updateUser_shouldReturn200() throws Exception {
        UserRequest request = new UserRequest("test@email.com", "pass123", "Ivo", "Ivić", "+38761111111", "GUEST");
        when(userService.updateUser(eq(1L), any(UserRequest.class))).thenReturn(createUserResponse());

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    // ==================== NEW: Pagination endpoint tests ====================

    @Test
    void getAllUsersPaginated_shouldReturn200WithPage() throws Exception {
        Page<UserResponse> page = new PageImpl<>(
                List.of(createUserResponse()),
                PageRequest.of(0, 10),
                1
        );
        when(userService.getAllUsersPaginated(any())).thenReturn(page);

        mockMvc.perform(get("/api/users/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "lastName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("test@email.com"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getAllUsersPaginated_shouldReturnEmptyPage() throws Exception {
        Page<UserResponse> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );
        when(userService.getAllUsersPaginated(any())).thenReturn(emptyPage);

        mockMvc.perform(get("/api/users/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ==================== NEW: Search endpoint tests ====================

    @Test
    void searchUsers_shouldReturn200_withRoleFilter() throws Exception {
        Page<UserResponse> page = new PageImpl<>(
                List.of(createUserResponse()),
                PageRequest.of(0, 10),
                1
        );
        when(userService.searchUsers(eq("GUEST"), eq(null), any())).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("role", "GUEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").value("GUEST"));
    }

    @Test
    void searchUsers_shouldReturn200_withRoleAndActiveFilter() throws Exception {
        Page<UserResponse> page = new PageImpl<>(
                List.of(createUserResponse()),
                PageRequest.of(0, 10),
                1
        );
        when(userService.searchUsers(eq("HOST"), eq(true), any())).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("role", "HOST")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void searchUsers_shouldReturn400_whenInvalidRole() throws Exception {
        when(userService.searchUsers(eq("INVALID"), eq(null), any()))
                .thenThrow(new IllegalArgumentException("Nevažeća uloga: INVALID"));

        mockMvc.perform(get("/api/users/search")
                        .param("role", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ==================== NEW: PATCH endpoint tests ====================

    @Test
    void patchUser_shouldReturn200_whenPartialUpdate() throws Exception {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .firstName("NovoIme")
                .build();

        UserResponse patchedResponse = createUserResponse();
        patchedResponse.setFirstName("NovoIme");

        when(userService.patchUser(eq(1L), any(UserPatchRequest.class))).thenReturn(patchedResponse);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("NovoIme"))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void patchUser_shouldReturn404_whenUserNotFound() throws Exception {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .firstName("NovoIme")
                .build();

        when(userService.patchUser(eq(99L), any(UserPatchRequest.class)))
                .thenThrow(new ResourceNotFoundException("Korisnik sa ID 99 nije pronađen"));

        mockMvc.perform(patch("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void patchUser_shouldReturn409_whenDuplicateEmail() throws Exception {
        UserPatchRequest patchRequest = UserPatchRequest.builder()
                .email("taken@email.com")
                .build();

        when(userService.patchUser(eq(1L), any(UserPatchRequest.class)))
                .thenThrow(new DuplicateResourceException("Email taken@email.com je već u upotrebi"));

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void patchUser_shouldReturn400_whenInvalidEmailFormat() throws Exception {
        // Send raw JSON with invalid email to trigger validation
        String invalidJson = "{\"email\": \"not-an-email\"}";

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserDetailsById_shouldReturn200() throws Exception {
        com.bookingnwt.userservice.dto.UserDetailsResponse detailsResponse = com.bookingnwt.userservice.dto.UserDetailsResponse.builder()
                .user(createUserResponse())
                .preference(new com.bookingnwt.userservice.dto.UserPreferenceResponse())
                .verifications(List.of())
                .build();
                
        when(userService.getUserDetailsById(1L)).thenReturn(detailsResponse);

        mockMvc.perform(get("/api/users/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@email.com"));
    }
}
