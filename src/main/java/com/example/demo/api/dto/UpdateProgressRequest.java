package com.example.demo.api.dto;

import jakarta.validation.constraints.Min;

public record UpdateProgressRequest(
    @Min(0) Integer currentWordIndex,
    Integer lastWpm
) {}