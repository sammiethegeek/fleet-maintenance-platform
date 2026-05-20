package com.fleet.maintenance.decision.dto;

import com.fleet.maintenance.shared.dto.DecisionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record DecisionRequest(
        @NotNull UUID maintenanceId,
        @NotNull DecisionType decisionType,
        @NotBlank @Size(max = 2000) String remarks,
        @NotNull LocalDateTime updatedOn
) {
}
