package com.example.demo.api.dto;

import java.time.LocalDateTime;

public record EndSessionResponse(
    Long sessionId,
    LocalDateTime endedAt,
    Integer avgWpm,
    Integer wordsRead
) {}