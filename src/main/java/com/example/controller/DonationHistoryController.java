package com.example.controller;

import com.example.model.DonationHistory;
import com.example.repository.DonationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
public class DonationHistoryController {

    @Autowired
    private DonationHistoryRepository donationHistoryRepository;
    @Autowired private com.example.security.SessionAuth auth;
    @Autowired private com.example.repository.UserRepository users;

    // Get all donation history for a donor
    @GetMapping("/{donorId}/donation-history")
    public List<DonationHistory> getDonationHistory(@PathVariable Long donorId, jakarta.servlet.http.HttpServletRequest request) {
        var user = auth.require(request, "donor", "admin");
        var donor = users.findById(donorId).orElseThrow(() -> new IllegalArgumentException("Donor not found."));
        auth.owner(user, donor.getEmail());
        return donationHistoryRepository.findByDonorId(donorId);
    }
}
