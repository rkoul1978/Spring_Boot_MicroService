package com.microservice.service;

import com.microservice.dto.UserDTO;
import com.microservice.kafka.KafkaUserEventProducer;
import com.microservice.model.User;
import com.microservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KafkaUserEventProducer kafkaUserEventProducer;

    /**
     * Create a new user and publish CREATED event
     * Evicts all related caches
     */
    @CacheEvict(value = {"users", "activeUsers"}, allEntries = true)
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating user with email: {}", userDTO.getEmail());
        User user = User.builder()
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .email(userDTO.getEmail())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .city(userDTO.getCity())
                .state(userDTO.getState())
                .zipCode(userDTO.getZipCode())
                .country(userDTO.getCountry())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        UserDTO createdUserDTO = convertToDTO(savedUser);
        
        // Publish user created event to Kafka
        kafkaUserEventProducer.publishUserCreatedEvent(createdUserDTO);
        
        log.info("User created successfully with ID: {}", savedUser.getId());
        return createdUserDTO;
    }

    /**
     * Get user by ID with caching
     */
    @Cacheable(value = "userById", key = "#id")
    public UserDTO getUserById(String id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new RuntimeException("User not found with ID: " + id);
                });
    }

    /**
     * Get user by email with caching
     */
    @Cacheable(value = "userByEmail", key = "#email")
    public UserDTO getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .map(this::convertToDTO)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new RuntimeException("User not found with email: " + email);
                });
    }

    /**
     * Get all users with caching
     */
    @Cacheable(value = "users")
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get users by city with caching
     */
    @Cacheable(value = "usersByCity", key = "#city")
    public List<UserDTO> getUsersByCity(String city) {
        log.info("Fetching users from city: {}", city);
        return userRepository.findByCity(city)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active users with caching
     */
    @Cacheable(value = "activeUsers")
    public List<UserDTO> getActiveUsers() {
        log.info("Fetching all active users");
        return userRepository.findByActive(true)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get users by name with caching
     */
    @Cacheable(value = "usersByName", key = "#firstName + '_' + #lastName")
    public List<UserDTO> getUsersByName(String firstName, String lastName) {
        log.info("Fetching users with name: {} {}", firstName, lastName);
        return userRepository.findByFirstNameAndLastName(firstName, lastName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update user and publish UPDATED event
     * Evicts related caches for the updated user
     */
    @CacheEvict(value = {"users", "userById", "userByEmail", "usersByCity", "activeUsers", "usersByName"}, 
                allEntries = true)
    public UserDTO updateUser(String id, UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setAddress(userDTO.getAddress());
        user.setCity(userDTO.getCity());
        user.setState(userDTO.getState());
        user.setZipCode(userDTO.getZipCode());
        user.setCountry(userDTO.getCountry());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        UserDTO updatedUserDTO = convertToDTO(updatedUser);
        
        // Publish user updated event to Kafka
        kafkaUserEventProducer.publishUserUpdatedEvent(updatedUserDTO);
        
        log.info("User updated successfully with ID: {}", id);
        return updatedUserDTO;
    }

    /**
     * Delete user and publish DELETED event
     * Evicts all related caches
     */
    @CacheEvict(value = {"users", "userById", "userByEmail", "usersByCity", "activeUsers", "usersByName"}, 
                allEntries = true)
    public void deleteUser(String id) {
        log.info("Deleting user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        userRepository.deleteById(id);
        
        // Publish user deleted event to Kafka
        kafkaUserEventProducer.publishUserDeletedEvent(convertToDTO(user));
        
        log.info("User deleted successfully with ID: {}", id);
    }

    /**
     * Deactivate user and publish DEACTIVATED event
     * Evicts all related caches
     */
    @CacheEvict(value = {"users", "userById", "userByEmail", "usersByCity", "activeUsers", "usersByName"}, 
                allEntries = true)
    public void deactivateUser(String id) {
        log.info("Deactivating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        User deactivatedUser = userRepository.save(user);
        
        // Publish user deactivated event to Kafka
        kafkaUserEventProducer.publishUserDeactivatedEvent(convertToDTO(deactivatedUser));
        
        log.info("User deactivated successfully with ID: {}", id);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .city(user.getCity())
                .state(user.getState())
                .zipCode(user.getZipCode())
                .country(user.getCountry())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
