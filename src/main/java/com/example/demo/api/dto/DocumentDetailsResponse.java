package com.example.demo.api.dto;

import java.time.LocalDateTime;

public record DocumentDetailsResponse(
        int id,
        String title,
        String status,
        String visibility,
        Boolean isSample,
        int wordCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer ownerUserId,
        Integer uploadedByAdminId
) {}