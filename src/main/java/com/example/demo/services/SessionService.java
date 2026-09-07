package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.error.NotFoundException;
import com.example.demo.model.Document;
import com.example.demo.model.ReadingSession;
import com.example.demo.model.User;
import com.example.demo.repositories.ReadingSessionRepository;


@Service
public class SessionService {
	
	@Autowired
	ReadingSessionRepository sessionRepo;
	
	@Transactional
    public ReadingSession start(User user, Document doc, String mode) {
        ReadingSession s = new ReadingSession();
        s.setUser(user);
        s.setDocument(doc);
        s.setMode(mode);
        s.setAvgWpm(60);;
        return sessionRepo.save(s);
    }
	
	@Transactional
    public void end(int userId, Long sessionId, Integer avgWpm, Integer wordsRead) {
        int updated = sessionRepo.endSession(userId, sessionId, avgWpm, wordsRead);
        if (updated == 0) throw new NotFoundException("Session not found");
    }
	
}
