package com.fleet.maintenance.decision.controller;

import com.fleet.maintenance.decision.dto.DecisionRequest;
import com.fleet.maintenance.decision.service.DecisionService;
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
public class DecisionController {
    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @PostMapping("/maintenance-requests/{id}/decision")
    public ResponseEntity<MaintenanceStatusResponse> decide(
            @PathVariable UUID id,
            @Valid @RequestBody DecisionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(decisionService.decide(id, request, principal));
    }
}
