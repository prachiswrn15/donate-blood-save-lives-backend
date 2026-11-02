package com.example.service;

import com.example.model.*;
import com.example.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service
public class WorkflowService {
    private final BloodRequestRepository requests;
    private final UserRepository users;
    private final HelpOfferRepository offers;
    private final DonationHistoryRepository history;
    public WorkflowService(BloodRequestRepository requests,UserRepository users,HelpOfferRepository offers,DonationHistoryRepository history) { this.requests=requests; this.users=users; this.offers=offers; this.history=history; }
    public BloodRequest get(Long id) { return requests.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Blood request not found.")); }
    public boolean admin(User user) { return user != null && "admin".equals(user.getRole()); }
    public boolean owner(User user,BloodRequest request) { return user != null && user.getEmail().equalsIgnoreCase(request.getRequestedBy() == null ? "" : request.getRequestedBy()); }
    public boolean assigned(User user,BloodRequest request) { return user != null && user.getEmail().equalsIgnoreCase(request.getAcceptedBy() == null ? "" : request.getAcceptedBy()); }
    public boolean published(BloodRequest request) { return Set.of("Open","Donor Assigned","Completed").contains(request.getStatus()); }
    public Map<String,Object> view(BloodRequest request,User user) {
        boolean privateView=admin(user) || owner(user,request) || assigned(user,request);
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("id",request.getId()); result.put("patientName",privateView ? request.getPatientName() : "Patient");
        result.put("bloodGroup",request.getBloodGroup()); result.put("location",request.getLocation()); result.put("hospital",request.getHospital()); result.put("urgency",request.getUrgency()); result.put("quantity",request.getQuantity()); result.put("reason",privateView ? request.getReason() : null); result.put("status",request.getStatus());
        if(privateView) { result.put("age",request.getAge()); result.put("gender",request.getGender()); result.put("requestedBy",request.getRequestedBy()); result.put("acceptedBy",request.getAcceptedBy()); result.put("contactNumber",request.getContactNumber()); }
        return result;
    }
    @Transactional public BloodRequest create(BloodRequest input,User receiver) {
        for(var entry : Map.of("Patient name",Objects.toString(input.getPatientName(),""),"City",Objects.toString(input.getLocation(),""),"Hospital",Objects.toString(input.getHospital(),""),"Reason",Objects.toString(input.getReason(),""),"Contact number",Objects.toString(input.getContactNumber(),"")).entrySet()) UserService.required(entry.getValue(),entry.getKey());
        if(!Set.of("A+","A-","B+","B-","AB+","AB-","O+","O-").contains(Objects.toString(input.getBloodGroup(),""))) throw new IllegalArgumentException("Choose a valid blood group.");
        if(input.getQuantity() == null || input.getQuantity() < 1 || input.getQuantity() > 100) throw new IllegalArgumentException("Quantity must be between 1 and 100 units.");
        if(!Set.of("Normal","High").contains(input.getUrgency())) throw new IllegalArgumentException("Choose Normal or High urgency.");
        if(input.getAge() == null || input.getAge() < 0 || input.getAge() > 120) throw new IllegalArgumentException("Enter a valid patient age.");
        if(!Set.of("Male","Female","Other").contains(Objects.toString(input.getGender(),""))) throw new IllegalArgumentException("Select patient gender.");
        String digits=input.getContactNumber().replaceAll("[^0-9]","");
        if(digits.length()<10 || digits.length()>15) throw new IllegalArgumentException("Enter a valid contact number.");
        BloodRequest saved=new BloodRequest();
        saved.setPatientName(input.getPatientName().trim()); saved.setAge(input.getAge()); saved.setGender(input.getGender()); saved.setBloodGroup(input.getBloodGroup()); saved.setLocation(input.getLocation().trim()); saved.setHospital(input.getHospital().trim()); saved.setContactNumber(input.getContactNumber().trim()); saved.setReason(input.getReason().trim()); saved.setUrgency(input.getUrgency()); saved.setQuantity(input.getQuantity()); saved.setRequestedBy(receiver.getEmail()); saved.setStatus("Pending Verification");
        return requests.save(saved);
    }
    public List<Map<String,Object>> visible(User user) {
        List<BloodRequest> visibleRequests = admin(user)
                ? requests.findAll()
                : requests.findByStatusIn(List.of("Open", "Donor Assigned", "Completed"));
        if (user != null && !admin(user)) {
            Map<Long, BloodRequest> byId = new LinkedHashMap<>();
            visibleRequests.forEach(request -> byId.put(request.getId(), request));
            requests.findByRequestedByIgnoreCase(user.getEmail()).forEach(request -> byId.put(request.getId(), request));
            visibleRequests = new ArrayList<>(byId.values());
        }
        return visibleRequests.stream().map(request -> view(request,user)).toList();
    }
    public List<Map<String,Object>> matching(User donor) {
        if(!donor.getIsAvailable()) return List.of();
        if (donor.getCity() == null || donor.getCity().isBlank()) return List.of();
        return requests.findByStatusAndBloodGroupAndLocationIgnoreCase("Open", donor.getBloodGroup(), donor.getCity().trim())
                .stream().map(request -> view(request,donor)).toList();
    }
    @Transactional public synchronized HelpOffer offer(Long requestId,User donor) {
        BloodRequest request=get(requestId);
        if(!"Open".equals(request.getStatus())) throw new IllegalArgumentException("Only open, approved requests can receive help offers.");
        if(!donor.getIsAvailable()) throw new IllegalArgumentException("Turn on your donor availability in Profile before offering help.");
        if(!Objects.equals(donor.getBloodGroup(),request.getBloodGroup())) throw new IllegalArgumentException("This request needs a different blood group.");
        HelpOffer offer=offers.findByRequestIdAndDonorId(requestId,donor.getId()).orElseGet(HelpOffer::new);
        if(offer.getId() != null && Set.of("Offered","Assigned","Completed").contains(offer.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT,"You have already offered help for this request.");
        offer.setRequestId(requestId); offer.setDonorId(donor.getId()); offer.setStatus("Offered"); return offers.save(offer);
    }
    public List<Map<String,Object>> offerViews(BloodRequest request,User user) {
        if(!admin(user) && !owner(user,request) && !assigned(user,request)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Help offers are private to this request.");
        return offerViews(offers.findByRequestId(request.getId()), user);
    }
    private List<Map<String,Object>> offerViews(List<HelpOffer> requestOffers, User viewer) {
        List<HelpOffer> visibleOffers = requestOffers.stream()
                .filter(offer -> !"Withdrawn".equals(offer.getStatus()))
                .toList();
        return mapOfferViews(visibleOffers, viewer);
    }
    private List<Map<String,Object>> mapOfferViews(List<HelpOffer> offerList, User viewer) {
        if (offerList.isEmpty()) return List.of();
        Map<Long, User> donorsById = users.findByIdIn(offerList.stream().map(HelpOffer::getDonorId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(User::getId, user -> user));
        Map<Long, BloodRequest> requestsById = requests.findByIdIn(offerList.stream().map(HelpOffer::getRequestId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(BloodRequest::getId, request -> request));
        return offerList.stream().map(offer -> offerView(offer, viewer, donorsById, requestsById)).toList();
    }
    private Map<String,Object> offerView(HelpOffer offer,User viewer,Map<Long, User> donorsById,Map<Long, BloodRequest> requestsById) {
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("id",offer.getId()); result.put("requestId",offer.getRequestId()); result.put("status",offer.getStatus()); result.put("createdAt",offer.getCreatedAt());
        User donor = donorsById.get(offer.getDonorId());
        if (donor != null) {
            result.put("donorName",donor.getName()); result.put("bloodGroup",donor.getBloodGroup()); result.put("city",donor.getCity());
            if(admin(viewer) || Objects.equals(viewer.getId(),donor.getId()) || Set.of("Assigned","Completed").contains(offer.getStatus())) { result.put("donorEmail",donor.getEmail()); result.put("phoneNumber",donor.getPhoneNumber()); }
        }
        BloodRequest request = requestsById.get(offer.getRequestId());
        if (request != null) result.put("request",view(request,viewer));
        return result;
    }
    public List<Map<String,Object>> myOffers(User donor) { return mapOfferViews(offers.findByDonorId(donor.getId()), donor); }
    @Transactional public synchronized BloodRequest assign(Long requestId,Long offerId) {
        BloodRequest request=get(requestId);
        if(!"Open".equals(request.getStatus())) throw new IllegalArgumentException("Approve the request before assigning a donor.");
        HelpOffer offer=offers.findById(offerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Help offer not found."));
        if(!Objects.equals(offer.getRequestId(),requestId) || !"Offered".equals(offer.getStatus())) throw new IllegalArgumentException("Choose an active offer for this request.");
        User donor=users.findById(offer.getDonorId()).orElseThrow();
        if(!"donor".equals(donor.getRole()) || !donor.getIsAvailable() || !Objects.equals(donor.getBloodGroup(),request.getBloodGroup())) throw new IllegalArgumentException("This donor is unavailable or has a different blood group.");
        if(offers.findByDonorId(donor.getId()).stream().anyMatch(item -> "Assigned".equals(item.getStatus()))) throw new IllegalArgumentException("This donor is already assigned to another request.");
        offers.findByRequestId(requestId).forEach(item -> { if("Offered".equals(item.getStatus())) { item.setStatus(Objects.equals(item.getId(),offerId) ? "Assigned" : "Not Selected"); offers.save(item); } });
        request.setAcceptedBy(donor.getEmail()); request.setStatus("Donor Assigned"); return requests.save(request);
    }
    @Transactional public synchronized BloodRequest transition(Long requestId,String next) {
        BloodRequest request=get(requestId); String status=request.getStatus();
        boolean allowed=("Pending Verification".equals(status) && Set.of("Open","Rejected","Cancelled").contains(next)) || ("Open".equals(status) && Set.of("Cancelled","Rejected").contains(next)) || ("Donor Assigned".equals(status) && Set.of("Completed","Cancelled","Open").contains(next));
        if(!allowed) throw new IllegalArgumentException("This status change is not allowed. Assign a donor before marking a request completed.");
        if("Completed".equals(next)) {
            User donor=users.findByEmailIgnoreCase(request.getAcceptedBy()).orElseThrow(() -> new IllegalArgumentException("Assigned donor account was not found."));
            DonationHistory entry=new DonationHistory(); entry.setDonorId(donor.getId()); entry.setBloodRequestId(requestId); entry.setDonationDate(new Date()); entry.setStatus("Completed"); history.save(entry);
            donor.setLastDonationDate(LocalDate.now()); donor.setIsAvailable(false); users.save(donor);
            // One completed donation ends the donor's other pending commitments.
            offers.findByDonorId(donor.getId()).forEach(item -> { if("Offered".equals(item.getStatus())) { item.setStatus("Withdrawn"); offers.save(item); } });
        }
        offers.findByRequestId(requestId).forEach(item -> { if("Assigned".equals(item.getStatus()) || "Offered".equals(item.getStatus())) { item.setStatus("Completed".equals(next) ? "Completed" : "Closed"); offers.save(item); } });
        if("Open".equals(next)) request.setAcceptedBy(null);
        request.setStatus(next); return requests.save(request);
    }
    @Transactional public BloodRequest cancel(Long id,User receiver) {
        BloodRequest request=get(id);
        if(!owner(receiver,request)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only cancel your own requests.");
        if(Set.of("Completed","Cancelled","Rejected").contains(request.getStatus())) throw new IllegalArgumentException("This request is already closed.");
        return transition(id,"Cancelled");
    }
    @Transactional public HelpOffer withdraw(Long offerId,User donor) {
        HelpOffer offer=offers.findById(offerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Offer not found."));
        if(!Objects.equals(offer.getDonorId(),donor.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You can only withdraw your own offers.");
        if(!"Offered".equals(offer.getStatus())) throw new IllegalArgumentException("Only pending offers can be withdrawn. Contact the administrator for assigned requests.");
        offer.setStatus("Withdrawn"); return offers.save(offer);
    }
}
