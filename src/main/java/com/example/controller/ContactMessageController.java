package com.example.controller;

import com.example.model.ContactMessage;
import com.example.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000") // frontend URL allow
@RestController
@RequestMapping("/api/contact")
public class ContactMessageController {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    // ✅ Save new contact message
    @PostMapping
    public ContactMessage saveMessage(@RequestBody ContactMessage contactMessage) {
        return contactMessageRepository.save(contactMessage);
    }

    // ✅ Get all contact messages
    @GetMapping
    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAll();
    }
}
