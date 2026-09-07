package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.api.error.ForbiddenException;
import com.example.demo.api.error.NotFoundException;
import com.example.demo.model.Document;
import com.example.demo.repositories.DocumentRepository;


@Service
public class DocumentAccessService {

	@Autowired
	DocumentRepository documentRepo;
	
	public Document requirePublic(int docId) {
        return documentRepo.findById(docId)
                .filter(d -> "PUBLIC".equals(d.getVisibility()))
                .filter(d -> "READY".equals(d.getStatus()))
                .orElseThrow(() -> new NotFoundException("Public document not found"));
    }
	
	public Document requirePrivateForUser(int docId, int userId) {
        return documentRepo.findById(docId)
                .filter(d -> "PRIVATE".equals(d.getVisibility()))
                .filter(d -> "READY".equals(d.getStatus()))
                .filter(d -> d.getOwner() != null && d.getOwner().getId() == userId)
                .orElseThrow(() -> new NotFoundException("Private document not found"));
    }
	
	public Document requireAccessibleForUser(int docId, int userId) {
        Document d = documentRepo.findById(docId)
                .filter(doc -> "READY".equals(doc.getStatus()))
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if ("PUBLIC".equals(d.getVisibility())) return d;

        if ("PRIVATE".equals(d.getVisibility())
                && d.getOwner().getId() == userId) return d;

        throw new ForbiddenException("No access to document");
    }
	
	
	
}
