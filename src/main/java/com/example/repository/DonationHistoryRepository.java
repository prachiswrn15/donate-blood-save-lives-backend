package com.example.repository;

import com.example.model.DonationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonationHistoryRepository extends JpaRepository<DonationHistory, Long> {
    List<DonationHistory> findByDonorId(Long donorId);
}
