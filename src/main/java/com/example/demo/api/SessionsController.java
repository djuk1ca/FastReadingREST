package com.example.demo.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.demo.api.dto.EndSessionRequest;
import com.example.demo.api.dto.StartSessionRequest;
import com.example.demo.api.dto.StartSessionResponse;
import com.example.demo.model.Document;
import com.example.demo.model.ReadingSession;
import com.example.demo.model.User;
import com.example.demo.security.CurrentUser;
import com.example.demo.services.DocumentAccessService;
import com.example.demo.services.SessionService;
import com.example.demo.services.UserService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/documents")
@Validated
public class SessionsController {

	@Autowired
    CurrentUser currentUser;
    @Autowired
	DocumentAccessService access;
    @Autowired
    SessionService sessions;
    @Autowired
    UserService userService;

    public SessionsController(CurrentUser currentUser,
                              DocumentAccessService access,
                              SessionService sessions) {
        this.currentUser = currentUser;
        this.access = access;
        this.sessions = sessions;
    }

    @PostMapping("/{docId}/sessions/start")
    public ResponseEntity<StartSessionResponse> start(@PathVariable int docId,
                                                     @Valid @RequestBody StartSessionRequest req,
                                                     Authentication auth) {
        int userId = currentUser.userId(auth);
        Document doc = access.requireAccessibleForUser(docId, userId);
        User user = userService.requireUser(userId);

        ReadingSession s = sessions.start(user, doc, req.mode());

        StartSessionResponse resp = new StartSessionResponse(
                s.getId(),
                doc.getId(),
                userId,
                s.getMode(),
                s.getStartedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/{docId}/sessions/{sessionId}/end")
    public ResponseEntity<Void> end(@PathVariable int docId,
                                    @PathVariable Long sessionId,
                                    @Valid @RequestBody EndSessionRequest req,
                                    Authentication auth) {
        int userId = currentUser.userId(auth);
        // docId nije striktno potreban za end, ali ok je da ga validiraš ako hoćeš:
        access.requireAccessibleForUser(docId, userId);

        sessions.end(userId, sessionId, req.avgWpm(), req.wordsRead());
        return ResponseEntity.noContent().build();
    }
}