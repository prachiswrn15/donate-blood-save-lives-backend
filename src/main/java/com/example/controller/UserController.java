package com.example.controller;

import com.example.model.User;
import com.example.model.LoginDTO;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Map;
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @Autowired
    private UserService userService;
 // Register API
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        Optional<User> existingUser = userService.getUserByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("Email already registered");
        }
        userService.saveUser(user);
        return ResponseEntity.ok("User registered successfully");
    }
    // Login API
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
        try {
            User existingUser = userService.loginUser(loginDTO.getEmail(), loginDTO.getPassword());
            if (existingUser != null) {
                return ResponseEntity.ok(existingUser);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }
 // Get User by Email
    @GetMapping("/email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("Email cannot be null or empty");
            }

            Optional<User> userOpt = userService.getUserByEmail(email);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("User not found with email: " + email);
            }
        } catch (Exception e) {
            e.printStackTrace(); // backend console me error dikhne ke liye
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Internal server error while fetching user");
        }
    }

    // Update Profile
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        if (updatedUser != null) {
            return ResponseEntity.ok("User updated successfully");
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }
    // Change Password
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        User user = userService.loginUser(email, currentPassword);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current password is incorrect");
        }
        user.setPassword(newPassword);
        userService.saveUser(user);
        return ResponseEntity.ok("Password changed successfully");
    }
    // Update Donor Availability
    @PutMapping("/{id}/availability")
    public ResponseEntity<User> updateAvailability(@PathVariable Long id, @RequestParam boolean isAvailable) {
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = optionalUser.get();
        user.setIsAvailable(isAvailable);
        User updatedUser = userService.saveUser(user);
        return ResponseEntity.ok(updatedUser);
    }
}
