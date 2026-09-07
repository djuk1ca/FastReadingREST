package com.example.demo.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.api.dto.DocumentUploadResponse;
import com.example.demo.security.CurrentUser;
import com.example.demo.services.DocumentIngestService;

@RestController
@RequestMapping("/admin/public-documents")
public class AdminDocumentsController {
	
	@Autowired
    DocumentIngestService ingest;
	
	@Autowired
    CurrentUser currentUser;  

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> upload(@RequestPart("file") MultipartFile file,
                                                        @RequestPart(value = "title", required = false) String title,
                                                        Authentication auth) {
        int adminId = currentUser.userId(auth);
        DocumentUploadResponse resp = ingest.uploadPublicSample(file, title, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}