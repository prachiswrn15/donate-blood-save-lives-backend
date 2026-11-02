package com.example.security;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final Environment env; private final UserRepository users;
    public AdminBootstrap(Environment env,UserRepository users) { this.env=env; this.users=users; }
    public void run(ApplicationArguments args) {
        String email=env.getProperty("ADMIN_EMAIL"), password=env.getProperty("ADMIN_PASSWORD");
        if(email == null || password == null || email.isBlank()) return;
        UserService.validatePassword(password);
        var admins=users.findAll().stream().filter(user -> "admin".equals(user.getRole())).toList();
        if("true".equalsIgnoreCase(env.getProperty("ADMIN_RESET"))) {
            if(admins.size() != 1) throw new IllegalStateException("Admin recovery requires exactly one existing admin account.");
            User admin=admins.get(0);
            users.findByEmailIgnoreCase(email.trim()).filter(existing -> !existing.getId().equals(admin.getId()))
                    .ifPresent(existing -> { throw new IllegalArgumentException("That email belongs to a different account."); });
            admin.setEmail(email.trim().toLowerCase());
            admin.setPassword(Passwords.hash(password));
            users.save(admin);
            return;
        }
        if(!admins.isEmpty()) return;
        if(users.findByEmailIgnoreCase(email.trim()).isPresent()) throw new IllegalArgumentException("Use a separate email for the initial admin account.");
        users.save(new User("Website Administrator", email.trim().toLowerCase(), Passwords.hash(password), "", "", "", "admin"));
    }
}
