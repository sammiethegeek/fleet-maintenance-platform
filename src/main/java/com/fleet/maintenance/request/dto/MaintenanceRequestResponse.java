package com.fleet.maintenance.request.dto;

import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record MaintenanceRequestResponse(
        UUID maintenanceId,
        MaintenanceStatus status,
        String vehicleId,
        String requesterId,
        String description,
        String severity,
        String impact,
        Integer impactedPeopleCount,
        LocalDateTime createdOn,
        LocalDateTime updatedOn,
        String assignedTo,
        Double estimatedCost,
        String inspectionReport,
        LocalDateTime inspectedOn,
        LocalDateTime estimatedCompletionDate,
        String additionalDetails
) {
}
