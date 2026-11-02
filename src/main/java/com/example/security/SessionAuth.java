package com.example.security;

import com.example.model.User;
import com.example.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Arrays;

@Component
public class SessionAuth {
    private final UserRepository users;
    public SessionAuth(UserRepository users) { this.users = users; }
    public User optional(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null || !(session.getAttribute("userId") instanceof Long id)) return null;
        return users.findById(id).orElse(null);
    }
    public User require(HttpServletRequest request, String... roles) {
        User user = optional(request);
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again.");
        if (roles.length > 0 && Arrays.stream(roles).noneMatch(role -> role.equals(user.getRole()))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action is not available for your account.");
        return user;
    }
    public void owner(User user, String email) {
        if (!"admin".equals(user.getRole()) && !user.getEmail().equalsIgnoreCase(email == null ? "" : email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own information.");
    }
}
