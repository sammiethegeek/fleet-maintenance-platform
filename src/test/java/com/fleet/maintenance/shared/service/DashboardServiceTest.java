package com.fleet.maintenance.shared.service;

import static com.fleet.maintenance.TestFixtures.COORDINATOR;
import static com.fleet.maintenance.TestFixtures.PROVIDER;
import static com.fleet.maintenance.TestFixtures.inspectionReport;
import static com.fleet.maintenance.TestFixtures.maintenanceRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fleet.maintenance.inspection.repository.InspectionRepository;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.request.mapper.RequestMapper;
import com.fleet.maintenance.request.repository.RequestRepository;
import com.fleet.maintenance.shared.dto.DashboardInspection;
import com.fleet.maintenance.shared.dto.DashboardItem;
import com.fleet.maintenance.shared.dto.DashboardRequest;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock
    RequestRepository requestRepository;
    @Mock
    InspectionRepository inspectionRepository;
    @Mock
    RequestMapper requestMapper;

    DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(requestRepository, inspectionRepository, requestMapper);
    }

    @Test
    void should_ReturnCoordinatorDashboard_When_PrincipalIsCoordinator() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.PENDING_APPROVAL);
        var inspection = inspectionReport(id, 100.0);
        DashboardItem item = dashboardItem(request);
        when(requestRepository.findByRequesterId("coordinator")).thenReturn(List.of(request));
        when(inspectionRepository.findTopByMaintenanceRequestMaintenanceIdOrderByCreatedAtDesc(id)).thenReturn(Optional.of(inspection));
        when(requestMapper.toDashboardItem(request, inspection)).thenReturn(item);

        var response = dashboardService.getDashboard(COORDINATOR);

        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.items()).containsExactly(item);
        verify(requestRepository).findByRequesterId("coordinator");
    }

    @Test
    void should_ReturnProviderDashboard_When_PrincipalIsProvider() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.ASSIGNED);
        DashboardItem item = dashboardItem(request);
        when(requestRepository.findByAssignedTo("provider")).thenReturn(List.of(request));
        when(inspectionRepository.findTopByMaintenanceRequestMaintenanceIdOrderByCreatedAtDesc(id)).thenReturn(Optional.empty());
        when(requestMapper.toDashboardItem(request, null)).thenReturn(item);

        var response = dashboardService.getDashboard(PROVIDER);

        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.items()).containsExactly(item);
        verify(requestRepository).findByAssignedTo("provider");
    }

    private DashboardItem dashboardItem(MaintenanceRequest request) {
        return new DashboardItem(
                new DashboardRequest(
                        request.getMaintenanceId(),
                        request.getVehicleId(),
                        request.getStatus(),
                        request.getDescription(),
                        request.getCreatedOn(),
                        request.getRequesterName()
                ),
                new DashboardInspection(UUID.randomUUID(), 100.0, request.getCreatedOn())
        );
    }
}
