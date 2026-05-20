package com.fleet.maintenance.request.validation;

import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.dto.Role;
import com.fleet.maintenance.shared.exception.BadRequestException;
import com.fleet.maintenance.shared.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class RequestValidator {
    public void requireCoordinator(UserPrincipal principal) {
        if (principal.role() != Role.ROLE_COORDINATOR) {
            throw new AccessDeniedException("Only coordinators can perform this action");
        }
    }

    public void requireVisible(MaintenanceRequest request, UserPrincipal principal) {
        boolean coordinatorOwns = principal.role() == Role.ROLE_COORDINATOR && principal.id().equals(request.getRequesterId());
        boolean providerAssigned = principal.role() == Role.ROLE_PROVIDER && principal.id().equals(request.getAssignedTo());
        if (!coordinatorOwns && !providerAssigned) {
            throw new AccessDeniedException("Maintenance request is not visible to this user");
        }
    }

    public void validateAssignProvider(MaintenanceRequest request) {
        if (request.getStatus() != MaintenanceStatus.CREATED) {
            throw new BadRequestException("Invalid transition: provider assignment requires CREATED status");
        }
    }
}
