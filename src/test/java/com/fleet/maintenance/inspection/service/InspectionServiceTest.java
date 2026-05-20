package com.fleet.maintenance.inspection.service;

import static com.fleet.maintenance.TestFixtures.COORDINATOR;
import static com.fleet.maintenance.TestFixtures.PROVIDER;
import static com.fleet.maintenance.TestFixtures.inspectionReport;
import static com.fleet.maintenance.TestFixtures.inspectionRequest;
import static com.fleet.maintenance.TestFixtures.maintenanceRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fleet.maintenance.inspection.dto.InspectionReportRequest;
import com.fleet.maintenance.inspection.entity.InspectionReport;
import com.fleet.maintenance.inspection.mapper.InspectionMapper;
import com.fleet.maintenance.inspection.repository.InspectionRepository;
import com.fleet.maintenance.inspection.validation.InspectionValidator;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.request.service.RequestService;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.exception.BadRequestException;
import com.fleet.maintenance.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {
    @Mock
    RequestService requestService;
    @Mock
    InspectionRepository inspectionRepository;
    @Mock
    InspectionMapper inspectionMapper;

    InspectionService inspectionService;

    @BeforeEach
    void setUp() {
        inspectionService = new InspectionService(requestService, inspectionRepository, inspectionMapper, new InspectionValidator());
    }

    @Test
    void should_SubmitInspectionReport_When_RequestIsAssignedToProvider() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.ASSIGNED);
        InspectionReportRequest dto = inspectionRequest(id, 100.0);
        InspectionReport report = inspectionReport(id, 100.0);
        when(requestService.findEntity(id)).thenReturn(request);
        when(inspectionMapper.toEntity(dto)).thenReturn(report);

        var response = inspectionService.submitInspection(id, dto, PROVIDER);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.PENDING_APPROVAL);
        assertThat(report.getMaintenanceId()).isEqualTo(id);
        verify(inspectionRepository).save(report);
        verify(requestService).save(request);
    }

    @Test
    void should_SubmitInspectionReport_When_EstimatedCostIsZero() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.ASSIGNED);
        InspectionReportRequest dto = inspectionRequest(id, 0.0);
        when(requestService.findEntity(id)).thenReturn(request);
        when(inspectionMapper.toEntity(dto)).thenReturn(inspectionReport(id, 0.0));

        var response = inspectionService.submitInspection(id, dto, PROVIDER);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.PENDING_APPROVAL);
    }

    @Test
    void should_SubmitInspectionReport_When_EstimatedCostIsVeryLarge() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.ASSIGNED);
        InspectionReportRequest dto = inspectionRequest(id, 999_999_999.99);
        when(requestService.findEntity(id)).thenReturn(request);
        when(inspectionMapper.toEntity(dto)).thenReturn(inspectionReport(id, 999_999_999.99));

        var response = inspectionService.submitInspection(id, dto, PROVIDER);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.PENDING_APPROVAL);
    }

    @Test
    void should_ThrowError_When_RequestDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(requestService.findEntity(id)).thenThrow(new ResourceNotFoundException("Maintenance request not found: " + id));

        assertThatThrownBy(() -> inspectionService.submitInspection(id, inspectionRequest(id, 100.0), PROVIDER))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_ThrowError_When_ReportAlreadySubmitted() {
        UUID id = UUID.randomUUID();
        when(requestService.findEntity(id)).thenReturn(maintenanceRequest(id, MaintenanceStatus.PENDING_APPROVAL));

        assertThatThrownBy(() -> inspectionService.submitInspection(id, inspectionRequest(id, 100.0), PROVIDER))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid transition: inspection submission requires ASSIGNED status");
    }

    @Test
    void should_ThrowError_When_UserIsNotProvider() {
        UUID id = UUID.randomUUID();
        when(requestService.findEntity(id)).thenReturn(maintenanceRequest(id, MaintenanceStatus.ASSIGNED));

        assertThatThrownBy(() -> inspectionService.submitInspection(id, inspectionRequest(id, 100.0), COORDINATOR))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only providers can submit inspection reports");
    }

    @Test
    void should_ThrowError_When_ProviderIsNotAssigned() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.ASSIGNED);
        request.setAssignedTo("different-provider");
        when(requestService.findEntity(id)).thenReturn(request);

        assertThatThrownBy(() -> inspectionService.submitInspection(id, inspectionRequest(id, 100.0), PROVIDER))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Provider is not assigned to this request");
    }
}
