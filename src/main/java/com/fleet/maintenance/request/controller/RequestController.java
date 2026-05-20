package com.fleet.maintenance.request.controller;

import com.fleet.maintenance.request.dto.AssignProviderRequest;
import com.fleet.maintenance.request.dto.CreateRequestRequest;
import com.fleet.maintenance.request.dto.MaintenanceRequestResponse;
import com.fleet.maintenance.request.dto.MaintenanceStatusResponse;
import com.fleet.maintenance.request.service.RequestService;
import com.fleet.maintenance.shared.dto.DashboardResponse;
import com.fleet.maintenance.shared.security.UserPrincipal;
import com.fleet.maintenance.shared.service.DashboardService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestController {
    private final RequestService requestService;
    private final DashboardService dashboardService;

    public RequestController(RequestService requestService, DashboardService dashboardService) {
        this.requestService = requestService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getDashboard(principal));
    }

    @PostMapping("/maintenance-requests")
    public ResponseEntity<MaintenanceStatusResponse> create(
            @Valid @RequestBody CreateRequestRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.create(request, principal));
    }

    @GetMapping("/maintenance-requests/{id}")
    public ResponseEntity<MaintenanceRequestResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(requestService.getDetails(id, principal));
    }

    @PutMapping("/maintenance-requests/{id}/assign-provider")
    public ResponseEntity<MaintenanceStatusResponse> assignProvider(
            @PathVariable UUID id,
            @Valid @RequestBody AssignProviderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(requestService.assignProvider(id, request, principal));
    }
}
