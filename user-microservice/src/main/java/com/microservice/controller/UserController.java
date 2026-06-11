package com.microservice.controller;

import com.microservice.dto.UserDTO;
import com.microservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Create a new user
     * POST /v1/users
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        log.info("API call: Creating new user");
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Get user by ID
     * GET /v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        log.info("API call: Getting user by ID: {}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user by email
     * GET /v1/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.info("API call: Getting user by email: {}", email);
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users
     * GET /v1/users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("API call: Getting all users");
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get all active users
     * GET /v1/users/status/active
     */
    @GetMapping("/status/active")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        log.info("API call: Getting all active users");
        List<UserDTO> activeUsers = userService.getActiveUsers();
        return ResponseEntity.ok(activeUsers);
    }

    /**
     * Get users by city
     * GET /v1/users/city/{city}
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<UserDTO>> getUsersByCity(@PathVariable String city) {
        log.info("API call: Getting users by city: {}", city);
        List<UserDTO> users = userService.getUsersByCity(city);
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by name
     * GET /v1/users/search?firstName=John&lastName=Doe
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> getUsersByName(
            @RequestParam String firstName,
            @RequestParam String lastName) {
        log.info("API call: Searching users by name: {} {}", firstName, lastName);
        List<UserDTO> users = userService.getUsersByName(firstName, lastName);
        return ResponseEntity.ok(users);
    }

    /**
     * Update user
     * PUT /v1/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String id,
            @RequestBody UserDTO userDTO) {
        log.info("API call: Updating user with ID: {}", id);
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deactivate user
     * PATCH /v1/users/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        log.info("API call: Deactivating user with ID: {}", id);
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete user
     * DELETE /v1/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("API call: Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
