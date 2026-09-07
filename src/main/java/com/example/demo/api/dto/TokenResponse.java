package com.example.demo.api.dto;

public record TokenResponse(String accessToken, long expiresInSeconds) {}