package com.fleet.maintenance.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssignProviderRequest(
        @NotNull UUID maintenanceId,
        @NotBlank @Size(max = 200) String providerName,
        @NotBlank @Size(max = 100) String providerId,
        @NotNull LocalDateTime updatedOn
) {
}
