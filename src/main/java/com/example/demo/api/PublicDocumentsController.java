package com.example.demo.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.dto.DocumentDetailsResponse;
import com.example.demo.api.dto.OpenDocumentResponse;
import com.example.demo.security.CurrentUser;
import com.example.demo.services.DocumentQueryService;

@RestController
@RequestMapping("/public-documents")
public class PublicDocumentsController {
	
	@Autowired
	DocumentQueryService service;
	
	@Autowired
	CurrentUser currentUser;
	
	@GetMapping
    public ResponseEntity<List<DocumentDetailsResponse>> list() {
        return ResponseEntity.ok(service.listPublic());
    }

    @GetMapping("/{docId}/open")
    public ResponseEntity<OpenDocumentResponse> open(@PathVariable int docId,
                                                     @RequestParam(defaultValue = "800") int prefetchFrom,
                                                     Authentication auth) {
        int userId = currentUser.userId(auth);
        return ResponseEntity.ok(service.openPublic(docId, userId, prefetchFrom));
    }
	
}
