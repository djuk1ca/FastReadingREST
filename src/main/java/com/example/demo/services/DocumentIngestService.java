package com.example.demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.api.dto.DocumentUploadResponse;
import com.example.demo.model.Document;
import com.example.demo.model.DocumentChunk;
import com.example.demo.model.User;
import com.example.demo.repositories.DocumentChunkRepository;
import com.example.demo.repositories.DocumentRepository;


@Service
public class DocumentIngestService {

	@Autowired
	DocumentRepository documentRepo;
	
	@Autowired
	DocumentChunkRepository chunkRepo;
	
	@Autowired
	PdfChunkingService pdf;
	
	@Autowired
	UserService userService;
	
	
	@Transactional
	public DocumentUploadResponse uploadPrivate(MultipartFile file, String title, int userId) {
		User owner = userService.requireUser(userId);
		
		Document d = new Document();
		d.setTitle(title != null ? title : file.getOriginalFilename());
		d.setStatus("PROCESSING");
        d.setVisibility("PRIVATE");
        d.setSample(true);
        d.setOwner(owner);
        
        d = documentRepo.save(d);
        int chunkCount = ingestChunks(d, file);
        
        d.setStatus("READY");
        documentRepo.save(d);
        
        return new DocumentUploadResponse(d.getId(), d.getStatus(), d.getWordCount(), chunkCount);
	}

	@Transactional
    public DocumentUploadResponse uploadPublicSample(MultipartFile file, String title, int adminId) {
        User admin = userService.requireUser(adminId);

        Document d = new Document();
        d.setTitle(title != null ? title : file.getOriginalFilename());
        d.setStatus("PROCESSING");
        d.setVisibility("PUBLIC");
        d.setSample(true);
        d.setUploadedByAdmin(admin);  // uploaded_by_admin_id

        d = documentRepo.save(d);

        int chunkCount = ingestChunks(d, file);

        d.setStatus("READY");
        documentRepo.save(d);

        return new DocumentUploadResponse(d.getId(), d.getStatus(), d.getWordCount(), chunkCount);
    }
	
	private int ingestChunks(Document doc, MultipartFile file) {
        try {
            List<List<String>> chunks = pdf.extractWordChunks(file.getInputStream());
            System.out.println("Extracted chunks=" + chunks.size());
            System.out.println("First chunk words=" + (chunks.isEmpty() ? 0 : chunks.get(0).size()));
            if (chunks.isEmpty()) {
            	doc.setStatus("FAILED");
            	documentRepo.save(doc);
            	throw new RuntimeException("PDF contains no extractable text (maybe scanned PDF).");
            }

            int startIndex = 0;
            int chunkIndex = 0;
            int totalWords = 0;
            

            for (List<String> chunkWords : chunks) {
                String chunkText = String.join(" ", chunkWords);
                if (chunkWords == null || chunkWords.isEmpty()) {
                    continue; // ne ubacuj chunk sa 0 reci
                }
                DocumentChunk c = new DocumentChunk();
                c.setDocument(doc);
                c.setChunkIndex(chunkIndex);
                c.setStartWordIndex(startIndex);
                c.setWordCount(chunkWords.size());
                c.setChunkText(chunkText);

                chunkRepo.save(c);

                totalWords += chunkWords.size();
                startIndex += chunkWords.size();
                chunkIndex++;
            }
           
            doc.setWordCount(totalWords);
            documentRepo.save(doc);
            
            if (totalWords == 0) {
                doc.setStatus("FAILED");
                documentRepo.save(doc);
                throw new RuntimeException("No words extracted from PDF.");
            }

            return chunkIndex;
        } catch (Exception e) {
            doc.setStatus("FAILED");
            documentRepo.save(doc);
            throw new RuntimeException("Chunk ingest failed: " + e.getMessage(), e);
        }
    }
	
}
