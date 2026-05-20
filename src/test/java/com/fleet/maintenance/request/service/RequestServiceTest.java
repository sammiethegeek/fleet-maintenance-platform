package com.fleet.maintenance.request.service;

import static com.fleet.maintenance.TestFixtures.COORDINATOR;
import static com.fleet.maintenance.TestFixtures.PROVIDER;
import static com.fleet.maintenance.TestFixtures.assignProvider;
import static com.fleet.maintenance.TestFixtures.createRequest;
import static com.fleet.maintenance.TestFixtures.maintenanceRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fleet.maintenance.inspection.repository.InspectionRepository;
import com.fleet.maintenance.request.dto.CreateRequestRequest;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.request.mapper.RequestMapper;
import com.fleet.maintenance.request.repository.RequestRepository;
import com.fleet.maintenance.request.validation.RequestValidator;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.exception.BadRequestException;
import com.fleet.maintenance.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class RequestServiceTest {
    @Mock
    RequestRepository requestRepository;
    @Mock
    InspectionRepository inspectionRepository;
    @Mock
    RequestMapper requestMapper;

    RequestService requestService;

    @BeforeEach
    void setUp() {
        requestService = new RequestService(requestRepository, inspectionRepository, requestMapper, new RequestValidator());
    }

    @Test
    void should_CreateRequest_When_DataIsValid() {
        CreateRequestRequest dto = createRequest();
        MaintenanceRequest mapped = maintenanceRequest(UUID.randomUUID(), null);
        when(requestMapper.toEntity(dto)).thenReturn(mapped);
        when(requestRepository.save(any(MaintenanceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = requestService.create(dto, COORDINATOR);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.CREATED);
        assertThat(mapped.getRequesterId()).isEqualTo("coordinator");
        assertThat(mapped.getRequesterName()).isEqualTo("Coordinator");
    }

    @Test
    void should_ThrowError_When_NonCoordinatorCreatesRequest() {
        assertThatThrownBy(() -> requestService.create(createRequest(), PROVIDER))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Only coordinators can perform this action");
    }

    @Test
    void should_AssignProvider_When_RequestIsCreatedAndVisible() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.CREATED);
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));

        var response = requestService.assignProvider(id, assignProvider(id), COORDINATOR);

        assertThat(response.status()).isEqualTo(MaintenanceStatus.ASSIGNED);
        assertThat(request.getAssignedTo()).isEqualTo("provider");
        assertThat(request.getProviderName()).isEqualTo("Provider");
        verify(requestRepository).save(request);
    }

    @Test
    void should_ThrowError_When_AssigningAlreadyAssignedRequest() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.of(maintenanceRequest(id, MaintenanceStatus.ASSIGNED)));

        assertThatThrownBy(() -> requestService.assignProvider(id, assignProvider(id), COORDINATOR))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid transition: provider assignment requires CREATED status");
    }

    @Test
    void should_ThrowError_When_AssigningNonExistentProviderRequest() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.assignProvider(id, assignProvider(id), COORDINATOR))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Maintenance request not found");
    }

    @Test
    void should_ThrowError_When_CoordinatorDoesNotOwnRequest() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.CREATED);
        request.setRequesterId("other-coordinator");
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.assignProvider(id, assignProvider(id), COORDINATOR))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Maintenance request is not visible to this user");
    }

    @Test
    void should_ReturnDetails_When_RequestIsVisibleToProvider() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.ASSIGNED);
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));

        requestService.getDetails(id, PROVIDER);

        verify(requestMapper).toResponse(request, null);
    }

    @Test
    void should_FindEntity_When_RequestExists() {
        UUID id = UUID.randomUUID();
        MaintenanceRequest request = maintenanceRequest(id, MaintenanceStatus.CREATED);
        when(requestRepository.findById(id)).thenReturn(Optional.of(request));

        assertThat(requestService.findEntity(id)).isSameAs(request);
    }

    @Test
    void should_ThrowError_When_RequestIsNotFound() {
        UUID id = UUID.randomUUID();
        when(requestRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.findEntity(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_HandleConcurrentRequestCreation_When_RepositoryAcceptsAll() throws Exception {
        int count = 10;
        CreateRequestRequest dto = createRequest();
        when(requestMapper.toEntity(dto)).thenAnswer(invocation -> maintenanceRequest(UUID.randomUUID(), null));
        when(requestRepository.save(any(MaintenanceRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(count);
        var executor = Executors.newFixedThreadPool(count);

        for (int i = 0; i < count; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    assertThat(requestService.create(dto, COORDINATOR).status()).isEqualTo(MaintenanceStatus.CREATED);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        executor.shutdownNow();

        verify(requestRepository, times(count)).save(any(MaintenanceRequest.class));
    }
}
