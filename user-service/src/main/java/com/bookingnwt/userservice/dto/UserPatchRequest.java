package com.bookingnwt.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for PATCH /api/users/{id} — partial update.
 * All fields are optional. Only non-null fields will be applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPatchRequest {

    @Email(message = "Email format nije validan")
    private String email;

    @Size(min = 6, message = "Lozinka mora imati najmanje 6 karaktera")
    private String password;

    private String firstName;

    private String lastName;

    private String phone;

    private String role;
}
