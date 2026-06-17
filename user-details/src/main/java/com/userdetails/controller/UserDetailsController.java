package com.userdetails.controller;

import com.userdetails.dto.UserDetailsRequest;
import com.userdetails.model.UserDetails;
import com.userdetails.repository.UserDetailsRepository;
import com.userdetails.service.UserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for managing user details.
 */
@RestController
@RequestMapping("/api/v1/user-details")
@RequiredArgsConstructor
@Slf4j
public class UserDetailsController {

    private final UserDetailsService service;
    private final UserDetailsRepository repository;

    /**
     * Receives user-created notifications from the user-microservice.
     */
    @PostMapping
    public ResponseEntity<UserDetails> create(@Valid @RequestBody UserDetailsRequest request) {
        log.info("Received user details request for userId: {}", request.getUserId());
        UserDetails saved = service.createOrUpdate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<UserDetails>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetails> getByUserId(@PathVariable String userId) {
        return repository.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
