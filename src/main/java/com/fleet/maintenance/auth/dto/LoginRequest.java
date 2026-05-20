package com.fleet.maintenance.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 100) @Pattern(regexp = "^[A-Za-z0-9._@-]+$") String username,
        @NotBlank @Size(max = 100) String password
) {
}
