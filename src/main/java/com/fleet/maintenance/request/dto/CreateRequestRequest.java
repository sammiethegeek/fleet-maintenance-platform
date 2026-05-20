package com.fleet.maintenance.request.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateRequestRequest(
        @NotBlank @Size(max = 100) String vehicleId,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 50) String severity,
        @NotBlank @Size(max = 1000) String impact,
        @NotNull @Min(0) Integer impactedPeopleCount,
        @NotNull LocalDateTime createdOn
) {
}
