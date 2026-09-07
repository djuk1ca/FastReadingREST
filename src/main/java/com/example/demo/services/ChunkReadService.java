package com.example.demo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.api.dto.ChunkAtResponse;
import com.example.demo.api.dto.ChunkPayloadResponse;
import com.example.demo.api.error.NotFoundException;
import com.example.demo.model.Document;
import com.example.demo.model.DocumentChunk;
import com.example.demo.repositories.DocumentChunkRepository;


@Service
public class ChunkReadService {

	public static final int PREFETCH_FROM_DEFAULT = 800;
	
	@Autowired
	DocumentChunkRepository chunkRepo;
	
	public ChunkAtResponse chunkAt(Document doc, int wordIndex, int prefetchFrom) {
		List<DocumentChunk> list = chunkRepo.findChunkAtOrBefore(doc.getId(), wordIndex, PageRequest.of(0, 1));
		
		if(list.isEmpty()) throw new NotFoundException("No chunks for document");
		
		DocumentChunk current = list.get(0);
		
		int offset = wordIndex - current.getStartWordIndex();
		
		ChunkPayloadResponse currentPayload = map(current);
		
		ChunkPayloadResponse nextPayload = null;
		if(offset >= prefetchFrom) {
			chunkRepo.findByDocumentIdAndChunkIndex(doc.getId(), current.getChunkIndex() + 1)
						.ifPresent(next -> {});
			DocumentChunk next = chunkRepo.findByDocumentIdAndChunkIndex(doc.getId(), current.getChunkIndex() + 1).orElse(null);
			if(next != null) nextPayload = map(next);
		}
		
		return new ChunkAtResponse(
				doc.getId(),
				wordIndex,
                current.getChunkIndex(),
                offset,
                currentPayload,
                nextPayload
		);
	}
	
	private ChunkPayloadResponse map(DocumentChunk c) {
        return new ChunkPayloadResponse (
                c.getChunkIndex(),
                c.getStartWordIndex(),
                c.getWordCount(),
                c.getChunkText()
        );
    }
	
}
