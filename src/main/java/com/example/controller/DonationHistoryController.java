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

    // Get all donation history for a donor
    @GetMapping("/{donorId}/donation-history")
    public List<DonationHistory> getDonationHistory(@PathVariable Long donorId) {
        return donationHistoryRepository.findByDonorId(donorId);
    }
}
