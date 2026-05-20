package com.fleet.maintenance.inspection.controller;

import com.fleet.maintenance.inspection.dto.InspectionReportRequest;
import com.fleet.maintenance.inspection.service.InspectionService;
import com.fleet.maintenance.request.dto.MaintenanceStatusResponse;
import com.fleet.maintenance.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InspectionController {
    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @PostMapping("/maintenance-requests/{id}/inspection")
    public ResponseEntity<MaintenanceStatusResponse> submitInspection(
            @PathVariable UUID id,
            @Valid @RequestBody InspectionReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(inspectionService.submitInspection(id, request, principal));
    }
}
