package com.fleet.maintenance.integration;

import static com.fleet.maintenance.TestFixtures.COORDINATOR;
import static com.fleet.maintenance.TestFixtures.PROVIDER;
import static com.fleet.maintenance.TestFixtures.assignProvider;
import static com.fleet.maintenance.TestFixtures.createRequest;
import static com.fleet.maintenance.TestFixtures.decisionRequest;
import static com.fleet.maintenance.TestFixtures.inspectionRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fleet.maintenance.auth.dto.LoginRequest;
import com.fleet.maintenance.auth.entity.UserAccount;
import com.fleet.maintenance.auth.repository.UserRepository;
import com.fleet.maintenance.auth.service.AuthService;
import com.fleet.maintenance.decision.repository.DecisionRepository;
import com.fleet.maintenance.decision.service.DecisionService;
import com.fleet.maintenance.infra.kafka.PaymentEventPublisher;
import com.fleet.maintenance.inspection.repository.InspectionRepository;
import com.fleet.maintenance.inspection.service.InspectionService;
import com.fleet.maintenance.request.repository.RequestRepository;
import com.fleet.maintenance.request.service.RequestService;
import com.fleet.maintenance.shared.dto.DecisionType;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.dto.Role;
import com.fleet.maintenance.shared.exception.ResourceNotFoundException;
import com.fleet.maintenance.shared.security.JwtTokenUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@SpringBootTest(properties = {
        "aws.region=us-east-1",
        "aws.dynamodb.endpoint=http://localhost:8000",
        "jwt.secret=mysecretkey123456789012345678901234567890",
        "jwt.expiration=86400000"
})
@EnabledIfSystemProperty(named = "runDynamoIntegrationTests", matches = "true")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class DynamoDbWorkflowIntegrationTest {
    @Autowired
    AuthService authService;
    @Autowired
    RequestService requestService;
    @Autowired
    InspectionService inspectionService;
    @Autowired
    DecisionService decisionService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RequestRepository requestRepository;
    @Autowired
    InspectionRepository inspectionRepository;
    @Autowired
    DecisionRepository decisionRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @MockBean
    PaymentEventPublisher paymentEventPublisher;

    List<UUID> requestIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        userRepository.save(user("coordinator", "Coordinator", "coordinator123", Role.ROLE_COORDINATOR));
        userRepository.save(user("provider", "Provider", "provider123", Role.ROLE_PROVIDER));
    }

    @AfterEach
    void tearDown() {
        requestIds.forEach(id -> {
            requestRepository.delete(id);
            inspectionRepository.findAll().stream()
                    .filter(report -> id.equals(report.getMaintenanceId()))
                    .forEach(report -> inspectionRepository.delete(report.getInspectionId()));
            decisionRepository.findAll().stream()
                    .filter(decision -> id.equals(decision.getMaintenanceId()))
                    .forEach(decision -> decisionRepository.delete(decision.getDecisionId()));
        });
        userRepository.delete("coordinator");
        userRepository.delete("provider");
    }

    @Test
    void should_CompleteLoginFlow_When_UserExistsInDynamoDb() {
        var response = authService.login(new LoginRequest("coordinator", "coordinator123"));

        assertThat(response.token()).isNotBlank();
        assertThat(jwtTokenUtil.parseToken(response.token()).role()).isEqualTo(Role.ROLE_COORDINATOR);
        assertThat(userRepository.findByUsername("coordinator")).isPresent()
                .get().extracting(UserAccount::getPasswordHash).asString().startsWith("$2");
    }

    @Test
    void should_CompleteApprovalWorkflow_When_AllStepsAreValid() {
        UUID id = requestService.create(createRequest(), COORDINATOR).maintenanceId();
        requestIds.add(id);

        assertThat(requestService.assignProvider(id, assignProvider(id), COORDINATOR).status()).isEqualTo(MaintenanceStatus.ASSIGNED);
        assertThat(inspectionService.submitInspection(id, inspectionRequest(id, 200.0), PROVIDER).status()).isEqualTo(MaintenanceStatus.PENDING_APPROVAL);
        assertThat(decisionService.decide(id, decisionRequest(id, DecisionType.APPROVE, "Approved"), COORDINATOR).status()).isEqualTo(MaintenanceStatus.APPROVED);
        assertThat(requestRepository.findById(id)).isPresent().get().extracting("status").isEqualTo(MaintenanceStatus.APPROVED);
    }

    @Test
    void should_CompleteRejectionWorkflow_When_AllStepsAreValid() {
        UUID id = requestService.create(createRequest(), COORDINATOR).maintenanceId();
        requestIds.add(id);

        requestService.assignProvider(id, assignProvider(id), COORDINATOR);
        inspectionService.submitInspection(id, inspectionRequest(id, 200.0), PROVIDER);
        decisionService.decide(id, decisionRequest(id, DecisionType.REJECT, "Rejected with reason"), COORDINATOR);

        assertThat(requestRepository.findById(id)).isPresent().get().extracting("status").isEqualTo(MaintenanceStatus.REJECTED);
        assertThat(decisionRepository.findByMaintenanceRequestMaintenanceIdOrderByCreatedAtAsc(id))
                .singleElement().extracting("remarks").isEqualTo("Rejected with reason");
    }

    @Test
    void should_CreateRequestsConcurrently_When_DynamoDbAcceptsWrites() throws Exception {
        int count = 5;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(count);
        var executor = Executors.newFixedThreadPool(count);

        for (int i = 0; i < count; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    UUID id = requestService.create(createRequest(), COORDINATOR).maintenanceId();
                    synchronized (requestIds) {
                        requestIds.add(id);
                    }
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

        assertThat(requestIds).hasSize(count);
        assertThat(requestIds).allSatisfy(id -> assertThat(requestRepository.findById(id)).isPresent());
    }

    @Test
    void should_HandleDynamoDbItemNotFound_When_RequestDoesNotExist() {
        assertThatThrownBy(() -> requestService.findEntity(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void should_PersistMultipleInspectionsForSameRequest_When_StatusAllowsSecondSubmission() {
        UUID id = requestService.create(createRequest(), COORDINATOR).maintenanceId();
        requestIds.add(id);
        requestService.assignProvider(id, assignProvider(id), COORDINATOR);
        inspectionService.submitInspection(id, inspectionRequest(id, 100.0), PROVIDER);
        var request = requestRepository.findById(id).orElseThrow();
        request.setStatus(MaintenanceStatus.ASSIGNED);
        requestRepository.save(request);

        inspectionService.submitInspection(id, inspectionRequest(id, 150.0), PROVIDER);

        assertThat(inspectionRepository.findAll().stream().filter(report -> id.equals(report.getMaintenanceId())).toList())
                .hasSize(2);
    }

    private UserAccount user(String username, String name, String rawPassword, Role role) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setUserId(username);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return user;
    }

    @TestConfiguration
    static class DynamoDbLocalConfig {
        @Bean
        @Primary
        DynamoDbClient dynamoDbClient() {
            return DynamoDbClient.builder()
                    .region(Region.US_EAST_1)
                    .endpointOverride(URI.create("http://localhost:8000"))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")))
                    .build();
        }
    }
}
