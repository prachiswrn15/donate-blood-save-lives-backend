package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Login
    public User loginUser(String email, String password) {
        if (email == null || password == null) return null;
        Optional<User> optionalUser = userRepository.findByEmailAndPassword(email, password);
        return optionalUser.orElse(null);
    }

    // 🔹 Get User by Email (null-safe)
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.isEmpty()) return Optional.empty();
        return userRepository.findByEmail(email);
    }

    // Get User by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Save User
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Update User Profile
    public User updateUser(User updatedUser) {
        Optional<User> existing = userRepository.findByEmail(updatedUser.getEmail());
        if (existing.isPresent()) {
            User user = existing.get();
            user.setName(updatedUser.getName());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            user.setCity(updatedUser.getCity());
            user.setBloodGroup(updatedUser.getBloodGroup());
            user.setRole(updatedUser.getRole());
            user.setIsAvailable(updatedUser.getIsAvailable()); // availability
            return userRepository.save(user);
        }
        return null;
    }
}
