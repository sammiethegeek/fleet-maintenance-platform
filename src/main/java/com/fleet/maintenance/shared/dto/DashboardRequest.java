package com.fleet.maintenance.shared.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DashboardRequest(
        UUID maintenanceId,
        String vehicleId,
        MaintenanceStatus status,
        String description,
        LocalDateTime createdOn,
        String requesterName
) {
}
