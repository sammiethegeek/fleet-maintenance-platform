package com.fleet.maintenance.request.repository;

import com.fleet.maintenance.request.entity.MaintenanceRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class RequestRepository {
    private final DynamoDbTable<MaintenanceRequest> table;

    public RequestRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("maintenance_requests", TableSchema.fromBean(MaintenanceRequest.class));
    }

    public MaintenanceRequest save(MaintenanceRequest request) {
        if (request.getCreatedAt() == null) {
            request.touchForCreate();
        } else {
            request.touchForUpdate();
        }
        table.putItem(request);
        return request;
    }

    public Optional<MaintenanceRequest> findById(UUID id) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id.toString()).build()));
    }

    public List<MaintenanceRequest> findAll() {
        return table.scan().items().stream()
                .sorted(Comparator.comparing(MaintenanceRequest::getCreatedOn).reversed())
                .toList();
    }

    public List<MaintenanceRequest> findByRequesterId(String requesterId) {
        return findAll().stream()
                .filter(request -> requesterId.equals(request.getRequesterId()))
                .toList();
    }

    public List<MaintenanceRequest> findByAssignedTo(String assignedTo) {
        return findAll().stream()
                .filter(request ->
                        assignedTo.equals(request.getAssignedTo()))
                .toList();
    }

    public void delete(UUID id) {
        table.deleteItem(Key.builder().partitionValue(id.toString()).build());
    }
}
