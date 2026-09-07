package com.example.demo.api.dto;

import jakarta.validation.constraints.Min;

public record EndSessionRequest(
    @Min(0) Integer avgWpm,
    @Min(0) Integer wordsRead
) {}