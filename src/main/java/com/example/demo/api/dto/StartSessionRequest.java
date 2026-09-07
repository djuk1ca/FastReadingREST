package com.example.demo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record StartSessionRequest(
    @NotBlank String mode // "SPEED" / "NORMAL"
) {}