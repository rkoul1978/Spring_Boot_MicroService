package com.userdetails.service;

import com.userdetails.dto.UserDetailsRequest;
import com.userdetails.model.UserDetails;
import com.userdetails.repository.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Business logic for storing and updating user details.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsService {

    private final UserDetailsRepository repository;

    /**
     * Create user details, or update the existing record if one already exists
     * for the given userId (idempotent against duplicate events).
     */
    public UserDetails createOrUpdate(UserDetailsRequest request) {
        UserDetails details = repository.findByUserId(request.getUserId())
                .map(existing -> applyUpdate(existing, request))
                .orElseGet(() -> applyCreate(request));

        UserDetails saved = repository.save(details);
        log.info("Stored user details for userId: {} ({})", saved.getUserId(), saved.getEmail());
        return saved;
    }

    private UserDetails applyCreate(UserDetailsRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return UserDetails.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private UserDetails applyUpdate(UserDetails existing, UserDetailsRequest request) {
        existing.setEmail(request.getEmail());
        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setPhoneNumber(request.getPhoneNumber());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setState(request.getState());
        existing.setZipCode(request.getZipCode());
        existing.setCountry(request.getCountry());
        existing.setUpdatedAt(LocalDateTime.now());
        return existing;
    }
}
