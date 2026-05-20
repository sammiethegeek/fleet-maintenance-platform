package com.fleet.maintenance.shared.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DashboardInspection(UUID inspectionId, Double estimatedAmount, LocalDateTime inspectionDate) {
}
