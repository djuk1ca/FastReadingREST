package com.example.demo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String identifier, // email ili username
    @NotBlank String password
) {}