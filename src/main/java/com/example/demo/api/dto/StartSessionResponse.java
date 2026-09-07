package com.example.demo.api.dto;

import java.time.LocalDateTime;

public record StartSessionResponse(
    Long sessionId,
    Integer documentId,
    Integer userId,
    String mode,
    LocalDateTime startedAt
) {}