package com.microservice.repository;

import com.microservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * 
 * Extends MongoRepository which provides basic CRUD operations:
 * - save(User)
 * - findById(String)
 * - findAll() - retrieves all users from the database
 * - delete(User)
 * - deleteById(String)
 * - count()
 * 
 * Custom query methods are defined below for advanced searches.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find a user by email address.
     * 
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users matching the given first and last name.
     * 
     * @param firstName the first name to search for
     * @param lastName the last name to search for
     * @return List of users matching the criteria
     */
    List<User> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Find all users located in a specific city.
     * 
     * @param city the city name to filter by
     * @return List of users in the specified city
     */
    List<User> findByCity(String city);

    /**
     * Find all users based on active status.
     * 
     * @param active true to get active users, false to get inactive users
     * @return List of users with the specified active status
     */
    List<User> findByActive(boolean active);

    /**
     * Find users by email using regex pattern matching (case-insensitive).
     * 
     * @param emailPattern the regex pattern to match against email addresses
     * @return List of users whose email matches the pattern
     */
    @Query("{ 'email' : { $regex: ?0, $options: 'i' } }")
    List<User> findByEmailRegex(String emailPattern);

}
