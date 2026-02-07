package com.example.service;

import com.example.model.BloodRequest;
import com.example.repository.BloodRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service
public class BloodRequestService {
    @Autowired
    private BloodRequestRepository bloodRequestRepository;
    public BloodRequest saveRequest(BloodRequest request) {
        if(request.getStatus() == null) {
            request.setStatus("Pending"); // Safety check
        }
        BloodRequest saved = bloodRequestRepository.save(request);
        System.out.println("Saved request ID: " + saved.getId()); // Debug
        return saved;
    }
    public List<BloodRequest> getAllRequests() {
        return bloodRequestRepository.findAll();
    }
    public Optional<BloodRequest> getRequestById(Long id) {
        return bloodRequestRepository.findById(id);
    }
    public void deleteRequest(Long id) {
        if (bloodRequestRepository.existsById(id)) {
            bloodRequestRepository.deleteById(id);
        } else {
            throw new RuntimeException("Request with ID " + id + " not found.");
        }
    }
    public BloodRequest updateStatus(Long id, String newStatus) {
        Optional<BloodRequest> optional = bloodRequestRepository.findById(id);
        if (optional.isPresent()) {
            BloodRequest req = optional.get();
            req.setStatus(newStatus);
            return bloodRequestRepository.save(req);
        }
        throw new RuntimeException("Request not found for id: " + id);
    }
    public BloodRequest acceptRequest(Long id, String donorEmail) {
        Optional<BloodRequest> optional = bloodRequestRepository.findById(id);
        if (optional.isPresent()) {
            BloodRequest req = optional.get();
            if ("Accepted".equalsIgnoreCase(req.getStatus())) {
                throw new RuntimeException("Request is already accepted by someone.");
            } 
            if ("Completed".equalsIgnoreCase(req.getStatus())) {
                throw new RuntimeException("Cannot accept a completed request.");
            }
            req.setStatus("Accepted");
            req.setAcceptedBy(donorEmail);
            return bloodRequestRepository.save(req);
        } else {
            throw new RuntimeException("Request not found for id: " + id);
        }
    }
    public List<BloodRequest> getRequestsByUsername(String usernameOrEmail) {
        return bloodRequestRepository.findByRequestedByIgnoreCase(usernameOrEmail.trim());
    }
}
