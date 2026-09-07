package com.example.demo.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.api.dto.UserResponse;
import com.example.demo.repositories.UserRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepo;

    public AdminController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> allUsers() {
        List<UserResponse> list = userRepo.findAll().stream()
            .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole()))
            .toList();
        return ResponseEntity.ok(list);
    }
}