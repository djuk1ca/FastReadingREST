package com.example.demo.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.dto.UpdateProgressRequest;
import com.example.demo.security.CurrentUser;
import com.example.demo.services.DocumentAccessService;
import com.example.demo.services.ProgressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/documents")
public class ProgressController {
	
	@Autowired
	CurrentUser currentUser;
	
	@Autowired
	DocumentAccessService access;
	
	@Autowired
	ProgressService progress;
	
	@PostMapping("/{docId}/progress")
	public ResponseEntity<?> update(@PathVariable int docId,
										@Valid @RequestBody UpdateProgressRequest req,
										Authentication auth) {
		int userId = currentUser.userId(auth);
		access.requireAccessibleForUser(docId, userId);
		
		progress.update(userId, docId, req.currentWordIndex(), req.lastWpm());
		return ResponseEntity.ok().build();
	}
}
