package com.userdetails.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Incoming payload describing a newly created user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @Email(message = "email must be valid")
    @NotBlank(message = "email is required")
    private String email;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
