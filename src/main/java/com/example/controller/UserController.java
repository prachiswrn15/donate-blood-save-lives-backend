package com.example.controller;
import com.example.model.User;
import com.example.model.LoginDTO;
import com.example.service.UserService;
import com.example.security.SessionAuth;
import com.example.security.Passwords;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController @RequestMapping("/api/users")
public class UserController {
    private final UserService users;
    private final SessionAuth auth;
    public UserController(UserService users, SessionAuth auth) { this.users=users; this.auth=auth; }
    @PostMapping("/register") public User register(@RequestBody User user) { return users.register(user); }
    @PostMapping("/login") public User login(@RequestBody LoginDTO input, HttpServletRequest request) {
        User user = users.loginUser(input.getEmail(),input.getPassword());
        if(user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password.");
        if(request.getSession(false) != null) request.getSession(false).invalidate();
        request.getSession(true).setAttribute("userId", user.getId());
        return user;
    }
    @GetMapping("/me") public User me(HttpServletRequest request) { return auth.require(request); }
    @PostMapping("/logout") public void logout(HttpServletRequest request) { request.getSession().invalidate(); }
    @GetMapping("/email") public User byEmail(@RequestParam String email, HttpServletRequest request) {
        User user = auth.require(request); auth.owner(user,email);
        return users.getUserByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found."));
    }
    @PutMapping("/update") public User update(@RequestBody User input, HttpServletRequest request) {
        User current=auth.require(request); input.setRole(current.getRole());
        return users.updateProfile(current,input);
    }
    @PutMapping("/change-password") public Map<String,String> changePassword(@RequestBody Map<String,String> input, HttpServletRequest request) {
        User user=auth.require(request);
        if(!Passwords.matches(input.get("currentPassword"),user.getPassword())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Current password is incorrect.");
        UserService.validatePassword(input.get("newPassword")); user.setPassword(Passwords.hash(input.get("newPassword"))); users.saveUser(user);
        return Map.of("message","Password changed successfully.");
    }
    @PutMapping("/{id}/availability") public User availability(@PathVariable Long id,@RequestParam boolean isAvailable,HttpServletRequest request) {
        User user=auth.require(request,"donor","admin");
        User donor=users.getUserById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Donor not found."));
        auth.owner(user,donor.getEmail());
        if(!"donor".equals(donor.getRole())) throw new IllegalArgumentException("Availability is only for donors.");
        donor.setIsAvailable(isAvailable); return users.saveUser(donor);
    }
}
