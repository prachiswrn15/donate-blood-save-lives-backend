package com.example.controller;

import com.example.model.ContactMessage;
import com.example.repository.ContactMessageRepository;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactMessageController {
    private final ContactMessageRepository messages;

    public ContactMessageController(ContactMessageRepository messages) {
        this.messages = messages;
    }

    @PostMapping
    public ContactMessage saveMessage(@RequestBody ContactMessage message) {
        UserService.required(message.getName(), "Name");
        UserService.required(message.getEmail(), "Email");
        UserService.required(message.getMessage(), "Message");
        if (message.getMessage().length() > 1000) {
            throw new IllegalArgumentException("Message must be 1000 characters or fewer.");
        }
        return messages.save(message);
    }

    @GetMapping
    public List<ContactMessage> getAllMessages() {
        return messages.findAll();
    }
}
