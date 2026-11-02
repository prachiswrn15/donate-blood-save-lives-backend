package com.example.controller;
import com.example.model.*;
import com.example.repository.*;
import com.example.service.WorkflowService;
import com.example.security.SessionAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/requests")
public class BloodRequestController {
    private final WorkflowService workflow; private final SessionAuth auth; private final BloodRequestRepository requests;
    public BloodRequestController(WorkflowService workflow,SessionAuth auth,BloodRequestRepository requests) { this.workflow=workflow; this.auth=auth; this.requests=requests; }
    @GetMapping public List<Map<String,Object>> all(HttpServletRequest request) { return workflow.visible(auth.optional(request)); }
    @PostMapping public Map<String,Object> create(@RequestBody BloodRequest input,HttpServletRequest request) { User user=auth.require(request,"patient"); return workflow.view(workflow.create(input,user),user); }
    @GetMapping("/{id}") public Map<String,Object> details(@PathVariable Long id,HttpServletRequest request) {
        User user=auth.optional(request); BloodRequest item=workflow.get(id);
        if(!workflow.published(item) && !workflow.admin(user) && !workflow.owner(user,item)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Blood request not found.");
        return workflow.view(item,user);
    }
    @GetMapping("/mine") public List<Map<String,Object>> mine(HttpServletRequest request) { User user=auth.require(request,"patient"); return requests.findByRequestedByIgnoreCase(user.getEmail()).stream().map(item -> workflow.view(item,user)).toList(); }
    @GetMapping("/user/{email}") public List<Map<String,Object>> byUser(@PathVariable String email,HttpServletRequest request) { User user=auth.require(request); auth.owner(user,email); return requests.findByRequestedByIgnoreCase(email).stream().map(item -> workflow.view(item,user)).toList(); }
    @GetMapping("/matching") public List<Map<String,Object>> matching(HttpServletRequest request) { return workflow.matching(auth.require(request,"donor")); }
    @PostMapping("/{id}/offers") public HelpOffer offer(@PathVariable Long id,HttpServletRequest request) { return workflow.offer(id,auth.require(request,"donor")); }
    @GetMapping("/{id}/offers") public List<Map<String,Object>> offers(@PathVariable Long id,HttpServletRequest request) { return workflow.offerViews(workflow.get(id),auth.require(request)); }
    @PutMapping("/{id}/assign/{offerId}") public Map<String,Object> assign(@PathVariable Long id,@PathVariable Long offerId,HttpServletRequest request) { User user=auth.require(request,"admin"); return workflow.view(workflow.assign(id,offerId),user); }
    @PutMapping("/{id}/status") public Map<String,Object> status(@PathVariable Long id,@RequestBody Map<String,String> body,HttpServletRequest request) { User user=auth.require(request,"admin"); String next=body.get("status"); if(next == null) throw new IllegalArgumentException("Status is required."); return workflow.view(workflow.transition(id,next),user); }
    @PutMapping("/{id}/cancel") public Map<String,Object> cancel(@PathVariable Long id,HttpServletRequest request) { User user=auth.require(request,"patient"); return workflow.view(workflow.cancel(id,user),user); }
    @DeleteMapping("/{id}") public void legacyDelete(@PathVariable Long id,HttpServletRequest request) { auth.require(request,"admin"); throw new IllegalArgumentException("Use Cancelled or Rejected status to keep the request history."); }
    @PutMapping("/{id}/accept") public void legacyAccept(@PathVariable Long id,HttpServletRequest request) { auth.require(request); throw new IllegalArgumentException("Donors must submit a help offer. Only the administrator can assign a donor."); }
}
