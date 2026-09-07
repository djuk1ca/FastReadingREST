package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repositories.ReadingProgressRepository;

import com.example.demo.model.Document;
import com.example.demo.model.ReadingProgress;
import com.example.demo.model.ReadingProgressId;
import com.example.demo.model.User;

@Service
public class ProgressService {

	@Autowired
	ReadingProgressRepository progressRepo;
	
	@Autowired
	UserService userService;
	
	@Transactional
	public ReadingProgress getOrCreate(Integer userId, Document doc) {
		return progressRepo.findByUserAndDocument(userId, doc.getId())
				.orElseGet(() -> {
					User u = userService.requireUser(userId);
					ReadingProgressId pk = new ReadingProgressId();
					pk.setUserId(userId);
					pk.setDocumentId(doc.getId());

					ReadingProgress rp = new ReadingProgress();
					rp.setId(pk);              // <-- naziv settera zavisi od tvoje klase (npr. setId / setReadingProgressPK)
					rp.setUser(u);
					rp.setDocument(doc);
					rp.setCurrentWordIndex(1);
					rp.setLastWpm(51);

					progressRepo.save(rp);
                    return progressRepo.save(rp);
				});
	}
	
	@Transactional
	public ReadingProgress update(Integer userId, Integer docId, Integer currentWordIndex, Integer lastWpm) {
		progressRepo.upsert(userId, docId, currentWordIndex, lastWpm);
		return progressRepo.findByUserAndDocument(userId, docId).orElseThrow(() -> new RuntimeException("Progress row missing after upsert"));
	}
}
