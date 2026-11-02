package com.example.DonateBlood.SaveLives;

import com.example.model.User;
import com.example.repository.*;
import com.example.security.Passwords;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:sqlite:./target/workflow-test.db", "spring.jpa.show-sql=false", "logging.level.root=ERROR", "ADMIN_EMAIL=", "ADMIN_PASSWORD="})
@AutoConfigureMockMvc
class DashboardRequestTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired BloodRequestRepository requests;
    @Autowired HelpOfferRepository offers;
    @Autowired DonationHistoryRepository history;
    @Autowired ContactMessageRepository messages;
    User receiver, donor, admin, otherReceiver;
    MockHttpSession receiverSession, donorSession, adminSession, otherSession;

    @BeforeEach void setup() throws Exception {
        offers.deleteAll(); history.deleteAll(); requests.deleteAll(); messages.deleteAll(); users.deleteAll();
        receiver=account("receiver@test.local","patient","B+","Pune");
        donor=account("donor@test.local","donor","B+","Pune");
        admin=account("admin@test.local","admin","","Pune");
        otherReceiver=account("other@test.local","patient","","Delhi");
        receiverSession=login(receiver); donorSession=login(donor); adminSession=login(admin); otherSession=login(otherReceiver);
    }
    User account(String email,String role,String group,String city) {
        return users.save(new User(role+" name", email, "test-password", group, city, "9876543210", role));
    }
    MockHttpSession login(User user) throws Exception {
        var result=mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("email",user.getEmail(),"password","test-password"))))
            .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
    Map<String,Object> input() {
        return new HashMap<>(Map.of("patientName","Patient Test","age",25,"gender","Female","bloodGroup","B+","quantity",2,"urgency","High","location","Pune","hospital","Test Hospital","contactNumber","9876543210","reason","Test coordination"));
    }
    JsonNode create() throws Exception {
        return body(mvc.perform(post("/api/requests").session(receiverSession).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Pending Verification")).andReturn().getResponse().getContentAsString());
    }
    JsonNode body(String text) throws Exception { return json.readTree(text); }
    void change(long id,String next) throws Exception {
        mvc.perform(put("/api/requests/"+id+"/status").session(adminSession).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("status",next)))).andExpect(status().isOk());
    }
    long offer(long id) throws Exception {
        return body(mvc.perform(post("/api/requests/"+id+"/offers").session(donorSession)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("id").asLong();
    }
    @Test void fullVerifiedDonationLifecycleAndPrivacy() throws Exception {
        long id=create().get("id").asLong();
        mvc.perform(get("/api/requests")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/requests/"+id)).andExpect(status().isNotFound());
        mvc.perform(post("/api/requests/"+id+"/offers").session(donorSession)).andExpect(status().isBadRequest());
        change(id,"Open");
        mvc.perform(get("/api/requests")).andExpect(jsonPath("$[0].contactNumber").doesNotExist()).andExpect(jsonPath("$[0].requestedBy").doesNotExist()).andExpect(jsonPath("$[0].patientName").value("Patient"));
        mvc.perform(get("/api/requests/matching").session(donorSession)).andExpect(jsonPath("$.length()").value(1));
        long offerId=offer(id);
        mvc.perform(post("/api/requests/"+id+"/offers").session(donorSession)).andExpect(status().isConflict());
        mvc.perform(get("/api/requests/"+id+"/offers").session(receiverSession)).andExpect(jsonPath("$[0].phoneNumber").doesNotExist()).andExpect(jsonPath("$[0].donorEmail").doesNotExist());
        mvc.perform(put("/api/requests/"+id+"/assign/"+offerId).session(adminSession)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Donor Assigned"));
        mvc.perform(get("/api/requests/"+id).session(donorSession)).andExpect(jsonPath("$.contactNumber").value("9876543210"));
        mvc.perform(get("/api/requests/"+id+"/offers").session(receiverSession)).andExpect(jsonPath("$[0].phoneNumber").value("9876543210"));
        change(id,"Completed");
        mvc.perform(get("/api/history/mine").session(donorSession)).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].bloodRequestId").value(id));
        assertFalse(users.findById(donor.getId()).orElseThrow().getIsAvailable());
        assertNotNull(users.findById(donor.getId()).orElseThrow().getLastDonationDate());
        mvc.perform(put("/api/requests/"+id+"/status").session(adminSession).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"Completed\"}")).andExpect(status().isBadRequest());
        assertEquals(1,history.count());
        mvc.perform(get("/api/admin/summary").session(adminSession)).andExpect(jsonPath("$.completedRequests").value(1)).andExpect(jsonPath("$.availableDonors").value(0));
    }
    @Test void ownershipAndRolesAreEnforcedOnServer() throws Exception {
        long id=create().get("id").asLong();
        mvc.perform(put("/api/requests/"+id+"/cancel").session(otherSession)).andExpect(status().isForbidden());
        mvc.perform(get("/api/requests/"+id+"/offers").session(otherSession)).andExpect(status().isForbidden());
        mvc.perform(put("/api/requests/"+id+"/status").session(donorSession).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"Open\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/users").session(donorSession)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/contact").session(receiverSession)).andExpect(status().isForbidden());
        mvc.perform(post("/api/requests").session(donorSession).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input()))).andExpect(status().isForbidden());
        mvc.perform(get("/api/users/email").session(donorSession).param("email",receiver.getEmail())).andExpect(status().isForbidden());
        var profile=new HashMap<String,Object>(Map.of("name","Updated Donor","email",donor.getEmail(),"city","Pune","phoneNumber","9876543210","bloodGroup","B+","role","admin","isAvailable",true));
        mvc.perform(put("/api/users/update").session(donorSession).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(profile))).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("donor"));
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("name","Fake Admin","email","fake@test.local","password","test-password","city","Pune","phoneNumber","9876543210","role","admin")))).andExpect(status().isBadRequest());
        mvc.perform(put("/api/requests/"+id+"/cancel").session(receiverSession)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("Cancelled"));
    }
    @Test void availabilityAndMatchingAndWithdrawal() throws Exception {
        long id=create().get("id").asLong(); change(id,"Open");
        donor.setCity("Delhi"); users.save(donor);
        mvc.perform(get("/api/requests/matching").session(donorSession)).andExpect(jsonPath("$.length()").value(0));
        donor.setCity("pune"); donor.setIsAvailable(false); users.save(donor);
        mvc.perform(post("/api/requests/"+id+"/offers").session(donorSession)).andExpect(status().isBadRequest());
        donor.setIsAvailable(true); donor.setBloodGroup("A+"); users.save(donor);
        mvc.perform(post("/api/requests/"+id+"/offers").session(donorSession)).andExpect(status().isBadRequest());
        donor.setBloodGroup("B+"); users.save(donor);
        long offerId=offer(id);
        mvc.perform(put("/api/offers/"+offerId+"/withdraw").session(donorSession)).andExpect(status().isOk());
        mvc.perform(put("/api/requests/"+id+"/assign/"+offerId).session(adminSession)).andExpect(status().isBadRequest());
        assertEquals(offerId,offer(id));
        mvc.perform(put("/api/requests/"+id+"/status").session(adminSession).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"Completed\"}")).andExpect(status().isBadRequest());
    }
    @Test void registerLoginAndPasswordChangeAndLogout() throws Exception {
        assertTrue(users.findById(donor.getId()).orElseThrow().getPassword().startsWith("pbkdf2$"));
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("name","New Receiver","email","new@test.local","password","new-password","city","Pune","phoneNumber","9876543210","role","patient")))).andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        assertTrue(Passwords.matches("new-password",users.findByEmail("new@test.local").orElseThrow().getPassword()));
        mvc.perform(put("/api/users/change-password").session(donorSession).contentType(MediaType.APPLICATION_JSON).content("{\"currentPassword\":\"test-password\",\"newPassword\":\"changed-password\"}")).andExpect(status().isOk());
        assertTrue(Passwords.matches("changed-password",users.findById(donor.getId()).orElseThrow().getPassword()));
        mvc.perform(post("/api/users/logout").session(donorSession)).andExpect(status().isOk());
        mvc.perform(get("/api/offers/mine")).andExpect(status().isUnauthorized());
    }
    @Test void invalidRequestAndUntrustedOriginAreRejected() throws Exception {
        var payload=input(); payload.put("quantity",0);
        mvc.perform(post("/api/requests").session(receiverSession).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload))).andExpect(status().isBadRequest());
        payload=input(); payload.put("status","Completed"); payload.put("requestedBy",otherReceiver.getEmail());
        var result=body(mvc.perform(post("/api/requests").session(receiverSession).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals(receiver.getEmail(),result.get("requestedBy").asText()); assertEquals("Pending Verification",result.get("status").asText());
        mvc.perform(post("/api/requests").session(receiverSession).header("Origin","https://untrusted.example").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(input()))).andExpect(status().isForbidden());
        mvc.perform(get("/api/requests").header("Origin","http://localhost:3000")).andExpect(header().string("Access-Control-Allow-Credentials","true"));
    }
}
