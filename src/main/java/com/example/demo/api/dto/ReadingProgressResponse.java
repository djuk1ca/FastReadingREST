package com.example.demo.api.dto;

import java.time.LocalDateTime;

public record ReadingProgressResponse(
    Long documentId,
    Long userId,
    Long currentWordIndex,
    Integer lastWpm,
    LocalDateTime updatedAt
) {}