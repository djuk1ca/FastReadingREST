package com.example.demo.api.dto;

public record DocumentUploadResponse(
        int documentId,
        String status,
        int wordCount,
        int chunkCount
) {}