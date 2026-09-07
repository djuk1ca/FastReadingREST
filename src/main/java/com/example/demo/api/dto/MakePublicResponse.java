package com.example.demo.api.dto;

import java.time.LocalDateTime;

public record MakePublicResponse(
    Long documentId,
    String visibility,       // "PUBLIC"
    Boolean isSample,        // true
    Long uploadedByAdminId,
    LocalDateTime updatedAt
) {}