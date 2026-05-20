package com.fleet.maintenance.inspection.service;

import com.fleet.maintenance.inspection.dto.InspectionReportRequest;
import com.fleet.maintenance.inspection.entity.InspectionReport;
import com.fleet.maintenance.inspection.mapper.InspectionMapper;
import com.fleet.maintenance.inspection.repository.InspectionRepository;
import com.fleet.maintenance.inspection.validation.InspectionValidator;
import com.fleet.maintenance.request.dto.MaintenanceStatusResponse;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.request.service.RequestService;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.security.UserPrincipal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InspectionService {
    private final RequestService requestService;
    private final InspectionRepository inspectionRepository;
    private final InspectionMapper inspectionMapper;
    private final InspectionValidator inspectionValidator;

    public InspectionService(
            RequestService requestService,
            InspectionRepository inspectionRepository,
            InspectionMapper inspectionMapper,
            InspectionValidator inspectionValidator
    ) {
        this.requestService = requestService;
        this.inspectionRepository = inspectionRepository;
        this.inspectionMapper = inspectionMapper;
        this.inspectionValidator = inspectionValidator;
    }

    public MaintenanceStatusResponse submitInspection(UUID id, InspectionReportRequest dto, UserPrincipal principal) {
        MaintenanceRequest request = requestService.findEntity(id);
        inspectionValidator.validateSubmit(request, principal);
        InspectionReport report = inspectionMapper.toEntity(dto);
        report.setMaintenanceId(request.getMaintenanceId());
        inspectionRepository.save(report);
        request.setStatus(MaintenanceStatus.PENDING_APPROVAL);
        request.setUpdatedOn(dto.updatedOn());
        requestService.save(request);
        return new MaintenanceStatusResponse(request.getMaintenanceId(), request.getStatus());
    }
}
