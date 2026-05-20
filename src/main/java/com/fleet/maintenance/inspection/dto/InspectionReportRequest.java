package com.fleet.maintenance.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record InspectionReportRequest(
        @NotNull UUID maintenanceId,
        @NotNull LocalDateTime updatedOn,
        @NotBlank @Size(max = 4000) String inspectionReport,
        @NotNull @PositiveOrZero Double estimatedCost,
        @NotNull LocalDateTime inspectedOn,
        @NotNull LocalDateTime estimatedCompletionDate,
        @Size(max = 4000) String additionalDetails
) {
}
