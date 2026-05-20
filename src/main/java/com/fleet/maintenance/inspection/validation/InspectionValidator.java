package com.fleet.maintenance.inspection.validation;

import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.dto.Role;
import com.fleet.maintenance.shared.exception.BadRequestException;
import com.fleet.maintenance.shared.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class InspectionValidator {
    public void validateSubmit(MaintenanceRequest request, UserPrincipal principal) {
        if (principal.role() != Role.ROLE_PROVIDER) {
            throw new AccessDeniedException("Only providers can submit inspection reports");
        }
        if (!principal.id().equals(request.getAssignedTo())) {
            throw new AccessDeniedException("Provider is not assigned to this request");
        }
        if (request.getStatus() != MaintenanceStatus.ASSIGNED) {
            throw new BadRequestException("Invalid transition: inspection submission requires ASSIGNED status");
        }
    }
}
