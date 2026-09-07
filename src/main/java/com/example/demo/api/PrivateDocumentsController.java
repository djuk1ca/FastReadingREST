package com.example.demo.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.api.dto.DocumentDetailsResponse;
import com.example.demo.api.dto.DocumentUploadResponse;
import com.example.demo.api.dto.OpenDocumentResponse;
import com.example.demo.security.CurrentUser;
import com.example.demo.services.DocumentIngestService;
import com.example.demo.services.DocumentQueryService;

@RestController
@RequestMapping("/private-documents")

public class PrivateDocumentsController {

	@Autowired
	DocumentIngestService ingest;

	@Autowired
	DocumentQueryService service;
	
	@Autowired
	CurrentUser currentUser;
	
	@GetMapping
    public ResponseEntity<List<DocumentDetailsResponse>> list(Authentication auth) {
		int userId = currentUser.userId(auth);
        return ResponseEntity.ok(service.listPrivate(userId));
    }
	
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DocumentUploadResponse> upload(@RequestPart("file") MultipartFile file,
															@RequestParam(value = "title", required = false) String title,
															Authentication auth) {
		int userId = currentUser.userId(auth);
		DocumentUploadResponse resp = ingest.uploadPrivate(file, title, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(resp);
	}
	
	@GetMapping("/{docId}/open")
	public ResponseEntity<OpenDocumentResponse> open(@PathVariable int docId, 
														@RequestParam(defaultValue = "800") int prefetchFrom, 
														Authentication auth) {
		int userId = currentUser.userId(auth);
		return ResponseEntity.ok(service.openPrivate(docId, userId, prefetchFrom));
	}
	
	
}
