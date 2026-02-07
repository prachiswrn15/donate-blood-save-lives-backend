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
    	String dbPath = "/opt/render/project/src/blood_donation.db";
    	File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            return ResponseEntity.status(404)
                    .body(null);
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=blood_donation.db")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(dbFile));
    }
}
