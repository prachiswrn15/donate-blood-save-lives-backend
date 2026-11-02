package com.example.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="help_offer", uniqueConstraints=@UniqueConstraint(columnNames={"requestId", "donorId"}))
public class HelpOffer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="requestId") private Long requestId;
    @Column(name="donorId") private Long donorId;
    private String status = "Offered";
    private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long value) { requestId=value; }
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long value) { donorId=value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status=value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
