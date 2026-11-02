package com.example.controller;
import com.example.model.User;
import com.example.repository.*;
import com.example.service.WorkflowService;
import com.example.security.SessionAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class CommunityController {
    private final SessionAuth auth; private final WorkflowService workflow; private final UserRepository users; private final BloodRequestRepository requests; private final DonationHistoryRepository history;
    public CommunityController(SessionAuth auth,WorkflowService workflow,UserRepository users,BloodRequestRepository requests,DonationHistoryRepository history) { this.auth=auth; this.workflow=workflow; this.users=users; this.requests=requests; this.history=history; }
    @GetMapping("/api/offers/mine") public List<Map<String,Object>> myOffers(HttpServletRequest request) { return workflow.myOffers(auth.require(request,"donor")); }
    @PutMapping("/api/offers/{id}/withdraw") public Object withdraw(@PathVariable Long id,HttpServletRequest request) { return workflow.withdraw(id,auth.require(request,"donor")); }
    @GetMapping("/api/history/mine") public Object history(HttpServletRequest request) { return history.findByDonorId(auth.require(request,"donor").getId()); }
    @GetMapping("/api/admin/users") public List<User> users(HttpServletRequest request) { auth.require(request,"admin"); return users.findAll(); }
    @PutMapping("/api/admin/donors/{id}/availability") public User availability(@PathVariable Long id,@RequestBody Map<String,Boolean> input,HttpServletRequest request) {
        auth.require(request,"admin"); User donor=users.findById(id).orElseThrow(() -> new IllegalArgumentException("Donor not found."));
        if(!"donor".equals(donor.getRole())) throw new IllegalArgumentException("This account is not a donor.");
        if(!input.containsKey("isAvailable") || input.get("isAvailable") == null) throw new IllegalArgumentException("Availability is required.");
        donor.setIsAvailable(input.get("isAvailable")); return users.save(donor);
    }
    @GetMapping("/api/admin/summary") public Map<String,Object> summary(HttpServletRequest request) {
        auth.require(request,"admin"); var allRequests=requests.findAll(); var donors=users.findAll().stream().filter(user -> "donor".equals(user.getRole())).toList();
        return Map.of("totalRequests",allRequests.size(),"pendingVerification",allRequests.stream().filter(item -> "Pending Verification".equals(item.getStatus())).count(),"openRequests",allRequests.stream().filter(item -> "Open".equals(item.getStatus())).count(),"assignedRequests",allRequests.stream().filter(item -> "Donor Assigned".equals(item.getStatus())).count(),"completedRequests",allRequests.stream().filter(item -> "Completed".equals(item.getStatus())).count(),"totalDonors",donors.size(),"availableDonors",donors.stream().filter(User::getIsAvailable).count(),"totalUsers",users.count());
    }
}
