package com.userdetails.service;

import com.userdetails.dto.UserDetailsRequest;
import com.userdetails.model.UserDetails;
import com.userdetails.repository.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Business logic for storing and updating user details.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsService {

    private static final int ACCOUNT_NUMBER_LENGTH = 12;
    private static final int MAX_ACCOUNT_GENERATION_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();

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
                .accountNumber(generateUniqueAccountNumber())
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

    /**
     * Generate a random numeric account number that is not already used by another
     * record. Retries a few times in the unlikely event of a collision.
     */
    private String generateUniqueAccountNumber() {
        for (int attempt = 0; attempt < MAX_ACCOUNT_GENERATION_ATTEMPTS; attempt++) {
            String candidate = randomAccountNumber();
            if (!repository.existsByAccountNumber(candidate)) {
                return candidate;
            }
            log.warn("Generated account number {} already exists, retrying", candidate);
        }
        throw new IllegalStateException("Unable to generate a unique account number after "
                + MAX_ACCOUNT_GENERATION_ATTEMPTS + " attempts");
    }

    private String randomAccountNumber() {
        StringBuilder sb = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
        // First digit is 1-9 to keep a fixed length account number.
        sb.append(random.nextInt(9) + 1);
        for (int i = 1; i < ACCOUNT_NUMBER_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
