package com.fleet.maintenance.decision.validation;

import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.dto.Role;
import com.fleet.maintenance.shared.exception.BadRequestException;
import com.fleet.maintenance.shared.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class DecisionValidator {
    public void validateDecision(MaintenanceRequest request, UserPrincipal principal) {
        if (principal.role() != Role.ROLE_COORDINATOR) {
            throw new AccessDeniedException("Only coordinators can make service authorization decisions");
        }
        if (!principal.id().equals(request.getRequesterId())) {
            throw new AccessDeniedException("Coordinator does not own this request");
        }
        if (request.getStatus() != MaintenanceStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Invalid transition: decision requires PENDING_APPROVAL status");
        }
    }
}
