package com.example.demo.services;

import org.springframework.stereotype.Service;

import com.example.demo.api.error.NotFoundException;
import com.example.demo.model.User;
import com.example.demo.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) { this.userRepo = userRepo; }

    public User requireUser(int id) {
        return userRepo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    } 
}