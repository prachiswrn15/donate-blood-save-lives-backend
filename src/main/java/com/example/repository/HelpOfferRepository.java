package com.example.repository;
import com.example.model.HelpOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface HelpOfferRepository extends JpaRepository<HelpOffer,Long> {
    List<HelpOffer> findByRequestId(Long id);
    List<HelpOffer> findByDonorId(Long id);
    Optional<HelpOffer> findByRequestIdAndDonorId(Long requestId, Long donorId);
}
