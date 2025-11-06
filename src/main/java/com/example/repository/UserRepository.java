package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ Used for login (currently plain password, can be hashed in future)
    Optional<User> findByEmailAndPassword(String email, String password);

    // ✅ Get user by email
    Optional<User> findByEmail(String email);
}
