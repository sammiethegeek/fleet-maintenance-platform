package com.fleet.maintenance.request.service;

import com.fleet.maintenance.inspection.entity.InspectionReport;
import com.fleet.maintenance.inspection.repository.InspectionRepository;
import com.fleet.maintenance.request.dto.AssignProviderRequest;
import com.fleet.maintenance.request.dto.CreateRequestRequest;
import com.fleet.maintenance.request.dto.MaintenanceRequestResponse;
import com.fleet.maintenance.request.dto.MaintenanceStatusResponse;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.request.mapper.RequestMapper;
import com.fleet.maintenance.request.repository.RequestRepository;
import com.fleet.maintenance.request.validation.RequestValidator;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.exception.ResourceNotFoundException;
import com.fleet.maintenance.shared.security.UserPrincipal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RequestService {
    private static final Logger log = LoggerFactory.getLogger(RequestService.class);

    private final RequestRepository requestRepository;
    private final InspectionRepository inspectionRepository;
    private final RequestMapper requestMapper;
    private final RequestValidator requestValidator;

    public RequestService(
            RequestRepository requestRepository,
            InspectionRepository inspectionRepository,
            RequestMapper requestMapper,
            RequestValidator requestValidator
    ) {
        this.requestRepository = requestRepository;
        this.inspectionRepository = inspectionRepository;
        this.requestMapper = requestMapper;
        this.requestValidator = requestValidator;
    }

    public MaintenanceStatusResponse create(CreateRequestRequest dto, UserPrincipal principal) {
        requestValidator.requireCoordinator(principal);
        MaintenanceRequest request = requestMapper.toEntity(dto);
        request.setRequesterId(principal.id());
        request.setRequesterName(principal.name());
        request.setStatus(MaintenanceStatus.CREATED);
        MaintenanceRequest saved = requestRepository.save(request);
        log.info("Maintenance request {} created by {}", saved.getMaintenanceId(), principal.id());
        return new MaintenanceStatusResponse(saved.getMaintenanceId(), saved.getStatus());
    }

    public MaintenanceStatusResponse assignProvider(UUID id, AssignProviderRequest dto, UserPrincipal principal) {
        requestValidator.requireCoordinator(principal);
        MaintenanceRequest request = findEntity(id);
        requestValidator.requireVisible(request, principal);
        requestValidator.validateAssignProvider(request);
        request.setAssignedTo(dto.providerId());
        request.setProviderName(dto.providerName());
        request.setUpdatedOn(dto.updatedOn());
        request.setStatus(MaintenanceStatus.ASSIGNED);
        requestRepository.save(request);
        log.info("Provider {} assigned to maintenance request {}", dto.providerId(), id);
        return new MaintenanceStatusResponse(request.getMaintenanceId(), request.getStatus());
    }

    public MaintenanceRequestResponse getDetails(UUID id, UserPrincipal principal) {
        MaintenanceRequest request = findEntity(id);
        requestValidator.requireVisible(request, principal);
        InspectionReport inspection = inspectionRepository.findTopByMaintenanceRequestMaintenanceIdOrderByCreatedAtDesc(id).orElse(null);
        return requestMapper.toResponse(request, inspection);
    }

    public MaintenanceRequest findEntity(UUID id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found: " + id));
    }

    public MaintenanceRequest save(MaintenanceRequest request) {
        return requestRepository.save(request);
    }
}
