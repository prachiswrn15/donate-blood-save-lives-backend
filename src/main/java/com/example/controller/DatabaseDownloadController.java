package com.example.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;

@RestController
@RequestMapping("/api/db")
public class DatabaseDownloadController {

    @GetMapping("/download")
    public ResponseEntity<FileSystemResource> downloadDatabase() {

        // ✅ Render ke liye absolute path fix
        // Render me file root folder me hoti hai (same level as pom.xml)
        String dbPath = "./blood_donation.db";  // <-- yahi sahi path hai
        File dbFile = new File(dbPath);

        // 🔍 Agar file nahi milti to 404 return
        if (!dbFile.exists()) {
            return ResponseEntity.status(404)
                    .body(null);
        }

        // ✅ File mil gayi — ab download response bhejo
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=blood_donation.db")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(dbFile));
    }
}
