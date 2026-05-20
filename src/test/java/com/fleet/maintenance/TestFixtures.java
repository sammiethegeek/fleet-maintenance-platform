package com.fleet.maintenance;

import com.fleet.maintenance.auth.entity.UserAccount;
import com.fleet.maintenance.decision.dto.DecisionRequest;
import com.fleet.maintenance.decision.entity.Decision;
import com.fleet.maintenance.inspection.dto.InspectionReportRequest;
import com.fleet.maintenance.inspection.entity.InspectionReport;
import com.fleet.maintenance.request.dto.AssignProviderRequest;
import com.fleet.maintenance.request.dto.CreateRequestRequest;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.shared.dto.DecisionType;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.dto.Role;
import com.fleet.maintenance.shared.security.UserPrincipal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class TestFixtures {
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 20, 10, 0);
    public static final UserPrincipal COORDINATOR = new UserPrincipal("coordinator", "Coordinator", Role.ROLE_COORDINATOR);
    public static final UserPrincipal PROVIDER = new UserPrincipal("provider", "Provider", Role.ROLE_PROVIDER);

    private TestFixtures() {
    }

    public static UserAccount user(String username, String passwordHash, Role role) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setUserId(username);
        user.setName(username.substring(0, 1).toUpperCase() + username.substring(1));
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }

    public static CreateRequestRequest createRequest() {
        return new CreateRequestRequest("VH-1", "Brake failure", "HIGH", "Route down", 2, NOW);
    }

    public static AssignProviderRequest assignProvider(UUID id) {
        return new AssignProviderRequest(id, "Provider", "provider", NOW.plusMinutes(5));
    }

    public static MaintenanceRequest maintenanceRequest(UUID id, MaintenanceStatus status) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setMaintenanceId(id);
        request.setVehicleId("VH-1");
        request.setRequesterId("coordinator");
        request.setRequesterName("Coordinator");
        request.setDescription("Brake failure");
        request.setSeverity("HIGH");
        request.setImpact("Route down");
        request.setImpactedPeopleCount(2);
        request.setStatus(status);
        request.setCreatedOn(NOW);
        request.setUpdatedOn(NOW);
        if (status != MaintenanceStatus.CREATED) {
            request.setAssignedTo("provider");
            request.setProviderName("Provider");
        }
        return request;
    }

    public static InspectionReportRequest inspectionRequest(UUID id, double estimatedCost) {
        return new InspectionReportRequest(
                id,
                NOW.plusHours(1),
                "Pads and rotor need replacement",
                estimatedCost,
                NOW.plusMinutes(30),
                NOW.plusDays(1),
                "Parts available"
        );
    }

    public static InspectionReport inspectionReport(UUID maintenanceId, double estimatedCost) {
        InspectionReport report = new InspectionReport();
        report.setInspectionId(UUID.randomUUID());
        report.setMaintenanceId(maintenanceId);
        report.setInspectionReport("Pads and rotor need replacement");
        report.setEstimatedCost(estimatedCost);
        report.setInspectedOn(NOW.plusMinutes(30));
        report.setEstimatedCompletionDate(NOW.plusDays(1));
        report.setAdditionalDetails("Parts available");
        report.touchForCreate();
        return report;
    }

    public static DecisionRequest decisionRequest(UUID id, DecisionType type, String remarks) {
        return new DecisionRequest(id, type, remarks, NOW.plusHours(2));
    }

    public static Decision decision(UUID maintenanceId, DecisionType type, String remarks) {
        Decision decision = new Decision();
        decision.setDecisionId(UUID.randomUUID());
        decision.setMaintenanceId(maintenanceId);
        decision.setDecisionType(type);
        decision.setRemarks(remarks);
        decision.touchForCreate();
        return decision;
    }
}
