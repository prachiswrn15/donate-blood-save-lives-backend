package com.example.model;
import jakarta.persistence.*;
@Entity
@Table(name = "blood_request")
public class BloodRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "patient_name")
    private String patientName;
    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;
    @Column(name = "blood_group")
    private String bloodGroup;
    private String location;
    private String reason;
    @Column(name = "requested_by")
    private String requestedBy;
    @Column(name = "accepted_by")
    private String acceptedBy;
    private String status;  // Remove initial value here
    public BloodRequest() {
        this.status = "Pending";
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public String getAcceptedBy() { return acceptedBy; }
    public void setAcceptedBy(String acceptedBy) { this.acceptedBy = acceptedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
