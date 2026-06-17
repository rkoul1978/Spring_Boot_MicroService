package com.userdetails.repository;

import com.userdetails.model.UserDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for {@link UserDetails}.
 */
@Repository
public interface UserDetailsRepository extends MongoRepository<UserDetails, String> {

    Optional<UserDetails> findByUserId(String userId);

    boolean existsByUserId(String userId);

    boolean existsByAccountNumber(String accountNumber);
}
