package com.example.demo.api.dto;

public record OpenDocumentResponse(
    DocumentDetailsResponse document,
    Integer currentWordIndex,      // 0 ako prvi put
    Integer lastWpm,            // null ako prvi put
    ChunkAtResponse chunkAt
) {}