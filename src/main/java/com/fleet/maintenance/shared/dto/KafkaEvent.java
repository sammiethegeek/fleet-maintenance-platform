package com.fleet.maintenance.shared.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record KafkaEvent(UUID maintenanceId, String requesterId, EventType eventType, LocalDateTime timestamp) {
}
