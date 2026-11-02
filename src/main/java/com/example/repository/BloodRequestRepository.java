package com.example.repository;

import com.example.model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByRequestedByIgnoreCase(String requestedBy);
    List<BloodRequest> findByAcceptedByIgnoreCase(String acceptedBy);
    List<BloodRequest> findByStatusIn(List<String> statuses);
    List<BloodRequest> findByStatusAndBloodGroupAndLocationIgnoreCase(String status, String bloodGroup, String location);
    List<BloodRequest> findByIdIn(Collection<Long> ids);
}
