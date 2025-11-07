package com.example.controller;

import com.example.model.BloodRequest;
import com.example.service.BloodRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "https://donate-blood-frontend-lives.vercel.app")
@RestController
@RequestMapping("/api/requests")
public class BloodRequestController {

    @Autowired
    private BloodRequestService bloodRequestService;

    @PostMapping
    public BloodRequest saveRequest(@RequestBody BloodRequest request) {
        return bloodRequestService.saveRequest(request);
    }

    @GetMapping
    public List<BloodRequest> getAllRequests() {
        return bloodRequestService.getAllRequests();
    }

    @GetMapping("/{id}")
    public Optional<BloodRequest> getRequestById(@PathVariable Long id) {
        return bloodRequestService.getRequestById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long id) {
        try {
            bloodRequestService.deleteRequest(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete failed: " + e.getMessage());
        }
    }

    // ✅ Update status via JSON body { "status": "Completed" }
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            BloodRequest updated = bloodRequestService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ✅ Donor accepts (query param donorEmail)
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id, @RequestParam String donorEmail) {
        try {
          BloodRequest updated = bloodRequestService.acceptRequest(id, donorEmail);
          return ResponseEntity.ok(updated);
        } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ✅ Patient's own requests by email/username
    @GetMapping("/user/{username}")
    public ResponseEntity<List<BloodRequest>> getRequestsByUsername(@PathVariable String username) {
        List<BloodRequest> userRequests = bloodRequestService.getRequestsByUsername(username);
        return ResponseEntity.ok(userRequests);
    }
}
