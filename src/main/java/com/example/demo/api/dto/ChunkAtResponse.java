package com.example.demo.api.dto;

public record ChunkAtResponse(
    Integer documentId,
    Integer absoluteWordIndex,   // currentWordIndex iz progress-a
    Integer currentChunkIndex,
    Integer offsetInChunk,    // absoluteWordIndex - startWordIndex
    ChunkPayloadResponse currentChunk,
    ChunkPayloadResponse nextChunk // null ako nema / nije prefetch prag
) {}