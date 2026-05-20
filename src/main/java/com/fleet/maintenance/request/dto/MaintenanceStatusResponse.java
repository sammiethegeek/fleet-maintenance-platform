package com.fleet.maintenance.request.dto;

import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import java.util.UUID;

public record MaintenanceStatusResponse(UUID maintenanceId, MaintenanceStatus status) {
}
