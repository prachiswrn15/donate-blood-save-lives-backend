package com.example.model;
public class LoginResponseDTO {
    private String email;
    private String username;
    private String role;

    // Constructors
    public LoginResponseDTO() {}

    public LoginResponseDTO(String email, String username, String role) {
        this.email = email;
        this.username = username;
        this.role = role;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}





