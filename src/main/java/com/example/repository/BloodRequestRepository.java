package com.example.repository;

import com.example.model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByRequestedBy(String requestedBy);
    List<BloodRequest> findByRequestedByIgnoreCase(String requestedBy);
}
