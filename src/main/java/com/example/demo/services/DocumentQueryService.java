package com.example.demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.dto.ChunkAtResponse;
import com.example.demo.api.dto.DocumentDetailsResponse;

import com.example.demo.api.dto.OpenDocumentResponse;
import com.example.demo.model.Document;
import com.example.demo.model.ReadingProgress;
import com.example.demo.repositories.DocumentRepository;


@Service
public class DocumentQueryService {

	
	@Autowired
	DocumentRepository documentRepo;
	
	@Autowired
	DocumentAccessService access;
	
	@Autowired
	ProgressService progressService;
	
	@Autowired
	ChunkReadService chunkReadService;
	
	public List<DocumentDetailsResponse> listPublic() {
        return documentRepo.findPublicReady().stream().map(this::mapList).toList();
    }

    public List<DocumentDetailsResponse> listPrivate(int userId) {
        return documentRepo.findByUserId(userId).stream().map(this::mapList).toList();
    }
    
    @Transactional(readOnly = true)
    public OpenDocumentResponse openPublic(int docId, int userId, int prefetchFrom) {
        Document doc = access.requirePublic(docId);

        ReadingProgress rp = progressService.getOrCreate(userId, doc);
        int wordIndex = rp.getCurrentWordIndex();

        ChunkAtResponse chunkAt = chunkReadService.chunkAt(doc, wordIndex, prefetchFrom);

        return new OpenDocumentResponse(mapList(doc), wordIndex, rp.getLastWpm(), chunkAt);
    }
    
    @Transactional(readOnly = true)
    public OpenDocumentResponse openPrivate(int docId, int userId, int prefetchFrom) {
        Document doc = access.requirePrivateForUser(docId, userId);

        ReadingProgress rp = progressService.getOrCreate(userId, doc);
        int wordIndex = rp.getCurrentWordIndex();

        ChunkAtResponse chunkAt = chunkReadService.chunkAt(doc, wordIndex, prefetchFrom);

        return new OpenDocumentResponse(mapList(doc), wordIndex, rp.getLastWpm(), chunkAt);
    }
    
    private DocumentDetailsResponse mapList(Document d) {
        Integer ownerId = d.getOwner() != null ? d.getOwner().getId() : null;
        Integer adminId = d.getUploadedByAdmin() != null ? d.getUploadedByAdmin().getId() : null;
        return new DocumentDetailsResponse(
                d.getId(),
                d.getTitle(),
                d.getStatus(),
                d.getVisibility(),
                d.isSample(),
                d.getWordCount(),
                d.getCreatedAt(),
                d.getUpdatedAt(),
                ownerId,
                adminId
        );
    }
    
    


	
}
