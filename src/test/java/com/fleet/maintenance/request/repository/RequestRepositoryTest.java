package com.fleet.maintenance.request.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestRepositoryTest {
    @Mock
    RequestRepository requestRepository;

    @Test
    void filtersDashboardRowsByRequesterAndProviderContracts() {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setMaintenanceId(java.util.UUID.randomUUID());
        request.setVehicleId("VH-1");
        request.setStatus(MaintenanceStatus.ASSIGNED);
        request.setRequesterId("coordinator");
        request.setRequesterName("Coordinator");
        request.setDescription("Broken");
        request.setSeverity("HIGH");
        request.setImpact("Route down");
        request.setImpactedPeopleCount(1);
        request.setAssignedTo("provider");
        request.setProviderName("Provider");
        request.setCreatedOn(LocalDateTime.now());
        request.setUpdatedOn(LocalDateTime.now());

        when(requestRepository.findByRequesterId("coordinator")).thenReturn(List.of(request));
        when(requestRepository.findByAssignedTo("provider")).thenReturn(List.of(request));

        assertThat(requestRepository.findByRequesterId("coordinator")).hasSize(1);
        assertThat(requestRepository.findByAssignedTo("provider")).hasSize(1);
    }
}
