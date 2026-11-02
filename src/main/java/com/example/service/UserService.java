package com.example.service;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.repository.BloodRequestRepository;
import com.example.security.Passwords;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository users;
    private final BloodRequestRepository requests;
    public UserService(UserRepository users, BloodRequestRepository requests) {
        this.users = users;
        this.requests = requests;
    }
    public User loginUser(String email, String password) {
        if (email == null) return null;
        User user = users.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null || !Passwords.matches(password, user.getPassword())) return null;
        if (!user.getPassword().startsWith("pbkdf2$")) user.setPassword(Passwords.hash(password));
        if ("users".equals(user.getRole()) || "user".equals(user.getRole()) || "receiver".equals(user.getRole())) user.setRole("patient");
        return users.save(user);
    }
    public Optional<User> getUserByEmail(String email) { return email == null ? Optional.empty() : users.findByEmailIgnoreCase(email.trim()); }
    public Optional<User> getUserById(Long id) { return users.findById(id); }
    public User saveUser(User user) { return users.save(user); }
    public User register(User user) {
        if (!Set.of("donor", "patient").contains(user.getRole() == null ? "" : user.getRole())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose Donor or Receiver. Admin accounts are created separately.");
        validateProfile(user);
        validatePassword(user.getPassword());
        if (user.getLastDonationDate() != null && user.getLastDonationDate().isAfter(java.time.LocalDate.now())) throw new IllegalArgumentException("Last donation date cannot be in the future.");
        if (getUserByEmail(user.getEmail()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"This email is already registered.");
        User saved = new User(user.getName().trim(), user.getEmail().trim().toLowerCase(), Passwords.hash(user.getPassword()), user.getBloodGroup(), user.getCity().trim(), user.getPhoneNumber().trim(), user.getRole());
        saved.setIsAvailable("donor".equals(user.getRole()) && user.getIsAvailable());
        saved.setLastDonationDate(user.getLastDonationDate());
        return users.save(saved);
    }
    @Transactional
    public User updateProfile(User current, User input) {
        validateProfile(input);
        if (input.getLastDonationDate() != null && input.getLastDonationDate().isAfter(java.time.LocalDate.now())) throw new IllegalArgumentException("Last donation date cannot be in the future.");
        String updatedEmail = input.getEmail().trim().toLowerCase();
        users.findByEmailIgnoreCase(updatedEmail)
                .filter(existing -> !existing.getId().equals(current.getId()))
                .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered."); });

        String previousEmail = current.getEmail();
        if (!previousEmail.equalsIgnoreCase(updatedEmail)) {
            var ownedRequests = requests.findByRequestedByIgnoreCase(previousEmail);
            var assignedRequests = requests.findByAcceptedByIgnoreCase(previousEmail);
            ownedRequests.forEach(request -> request.setRequestedBy(updatedEmail));
            assignedRequests.forEach(request -> request.setAcceptedBy(updatedEmail));
            requests.saveAll(ownedRequests);
            requests.saveAll(assignedRequests);
        }

        current.setEmail(updatedEmail);
        current.setName(input.getName().trim()); current.setCity(input.getCity().trim()); current.setPhoneNumber(input.getPhoneNumber().trim()); current.setBloodGroup(input.getBloodGroup());
        if ("donor".equals(current.getRole())) { current.setIsAvailable(input.getIsAvailable()); current.setLastDonationDate(input.getLastDonationDate()); }
        return users.save(current);
    }
    public void validateProfile(User user) {
        required(user.getName(),"Name"); required(user.getCity(),"City"); required(user.getPhoneNumber(),"Contact number");
        if (user.getEmail() == null || !user.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Enter a valid email address.");
        if (user.getPhoneNumber().replaceAll("[^0-9]", "").length() < 10 || user.getPhoneNumber().replaceAll("[^0-9]", "").length() > 15) throw new IllegalArgumentException("Enter a valid contact number.");
        if ("donor".equals(user.getRole()) && !Set.of("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(user.getBloodGroup() == null ? "" : user.getBloodGroup())) throw new IllegalArgumentException("Choose a blood group.");
    }
    public static void required(String value, String field) { if(value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required."); }
    public static void validatePassword(String password) { if(password == null || password.length() < 8) throw new IllegalArgumentException("Password must contain at least 8 characters."); }
}
