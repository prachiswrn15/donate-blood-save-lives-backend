package com.example.model;
import jakarta.persistence.*;
import java.util.Date;
@Entity
@Table(name = "donation_history")
public class DonationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long donorId;
    private Long bloodRequestId;
    private Date donationDate;
    private String status;
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public Long getBloodRequestId() { return bloodRequestId; }
    public void setBloodRequestId(Long bloodRequestId) { this.bloodRequestId = bloodRequestId; }

    public Date getDonationDate() { return donationDate; }
    public void setDonationDate(Date donationDate) { this.donationDate = donationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
