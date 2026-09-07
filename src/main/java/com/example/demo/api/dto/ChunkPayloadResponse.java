package com.example.demo.api.dto;

public record ChunkPayloadResponse(
    Integer chunkIndex,
    Integer startWordIndex,
    Integer wordCount,
    String chunkText
) {}