package com.fleet.maintenance.decision.service;

import static com.fleet.maintenance.TestFixtures.COORDINATOR;
import static com.fleet.maintenance.TestFixtures.PROVIDER;
import static com.fleet.maintenance.TestFixtures.decision;
import static com.fleet.maintenance.TestFixtures.decisionRequest;
import static com.fleet.maintenance.TestFixtures.maintenanceRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fleet.maintenance.decision.dto.DecisionRequest;
import com.fleet.maintenance.decision.mapper.DecisionMapper;
import com.fleet.maintenance.decision.repository.DecisionRepository;
import com.fleet.maintenance.decision.validation.DecisionValidator;
import com.fleet.maintenance.infra.kafka.PaymentEventPublisher;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.request.service.RequestService;
import com.fleet.maintenance.shared.dto.DecisionType;
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
class DecisionServiceTest {
    @Mock
    RequestService requestService;
    @Mock
    DecisionRepository decisionRepository;
    @Mock
    DecisionMapper decisionMapper;
    @Mock
    PaymentEventPublisher paymentEventPublisher;

    DecisionService decisionService;

    @BeforeEach
    void setUp() {
        decisionService = new DecisionService(requestService, decisionRepository, decisionMapper,
                new DecisionValidator(), paymentEventPublisher);
    }

    @Test
    void should_ApproveInspection_When_RequestIsPendingApproval() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.PENDING_APPROVAL);
        DecisionRequest dto = decisionRequest(id, DecisionType.APPROVE, "Approved");
        when(requestService.findEntity(id)).thenReturn(request);
        when(decisionMapper.toEntity(dto)).thenReturn(decision(id, DecisionType.APPROVE, "Approved"));

        var response = decisionService.decide(id, dto, COORDINATOR);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.APPROVED);
        verify(paymentEventPublisher).publishPaymentInitiatedEvent(id, "coordinator");
        verify(requestService).save(request);
    }

    @Test
    void should_RejectInspection_When_RequestIsPendingApproval() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.PENDING_APPROVAL);
        DecisionRequest dto = decisionRequest(id, DecisionType.REJECT, "Insufficient findings");
        when(requestService.findEntity(id)).thenReturn(request);
        when(decisionMapper.toEntity(dto)).thenReturn(decision(id, DecisionType.REJECT, "Insufficient findings"));

        var response = decisionService.decide(id, dto, COORDINATOR);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.REJECTED);
        verify(paymentEventPublisher, never()).publishPaymentInitiatedEvent(id, "coordinator");
    }

    @Test
    void should_RequestMoreInfo_When_DecisionTypeIsRequestMoreInfo() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.PENDING_APPROVAL);
        DecisionRequest dto = decisionRequest(id, DecisionType.REQUEST_MORE_INFO, "Need photos");
        when(requestService.findEntity(id)).thenReturn(request);
        when(decisionMapper.toEntity(dto)).thenReturn(decision(id, DecisionType.REQUEST_MORE_INFO, "Need photos"));

        var response = decisionService.decide(id, dto, COORDINATOR);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.RFI_REQUESTED);
    }

    @Test
    void should_AllowNullCommentsAtServiceLayer_When_Approving() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.PENDING_APPROVAL);
        DecisionRequest dto = decisionRequest(id, DecisionType.APPROVE, null);
        when(requestService.findEntity(id)).thenReturn(request);
        when(decisionMapper.toEntity(dto)).thenReturn(decision(id, DecisionType.APPROVE, null));

        var response = decisionService.decide(id, dto, COORDINATOR);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.APPROVED);
    }

    @Test
    void should_ThrowError_When_RequestDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(requestService.findEntity(id)).thenThrow(new ResourceNotFoundException("Maintenance request not found: " + id));

        assertThatThrownBy(() -> decisionService.decide(id, decisionRequest(id, DecisionType.APPROVE, "Approved"), COORDINATOR))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_ThrowError_When_RequestHasNoInspection() {
        UUID id = UUID.randomUUID();
        when(requestService.findEntity(id)).thenReturn(maintenanceRequest(id, MaintenanceStatus.ASSIGNED));

        assertThatThrownBy(() -> decisionService.decide(id, decisionRequest(id, DecisionType.APPROVE, "Approved"), COORDINATOR))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid transition: decision requires PENDING_APPROVAL status");
    }

    @Test
    void should_ThrowError_When_RequestAlreadyDecided() {
        UUID id = UUID.randomUUID();
        when(requestService.findEntity(id)).thenReturn(maintenanceRequest(id, MaintenanceStatus.APPROVED));

        assertThatThrownBy(() -> decisionService.decide(id, decisionRequest(id, DecisionType.REJECT, "No"), COORDINATOR))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid transition: decision requires PENDING_APPROVAL status");
    }

    @Test
    void should_ThrowError_When_UserIsNotCoordinator() {
        UUID id = UUID.randomUUID();
        when(requestService.findEntity(id)).thenReturn(maintenanceRequest(id, MaintenanceStatus.PENDING_APPROVAL));

        assertThatThrownBy(() -> decisionService.decide(id, decisionRequest(id, DecisionType.APPROVE, "Approved"), PROVIDER))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only coordinators can make service authorization decisions");
    }

    @Test
    void should_ThrowError_When_CoordinatorDoesNotOwnRequest() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.PENDING_APPROVAL);
        request.setRequesterId("different-coordinator");
        when(requestService.findEntity(id)).thenReturn(request);

        assertThatThrownBy(() -> decisionService.decide(id, decisionRequest(id, DecisionType.APPROVE, "Approved"), COORDINATOR))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Coordinator does not own this request");
    }
}
