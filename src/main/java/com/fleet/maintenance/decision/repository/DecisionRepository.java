package com.fleet.maintenance.decision.repository;

import com.fleet.maintenance.decision.entity.Decision;
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
public class DecisionRepository {
    private final DynamoDbTable<Decision> table;

    public DecisionRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("decisions", TableSchema.fromBean(Decision.class));
    }

    public Decision save(Decision decision) {
        if (decision.getCreatedAt() == null) {
            decision.touchForCreate();
        } else {
            decision.touchForUpdate();
        }
        table.putItem(decision);
        return decision;
    }

    public Optional<Decision> findById(UUID id) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id.toString()).build()));
    }

    public List<Decision> findAll() {
        return table.scan().items().stream().toList();
    }

    public List<Decision> findByMaintenanceRequestMaintenanceIdOrderByCreatedAtAsc(UUID maintenanceId) {
        return findAll().stream()
                .filter(decision -> maintenanceId.equals(decision.getMaintenanceId()))
                .sorted(Comparator.comparing(Decision::getCreatedAt))
                .toList();
    }

    public void delete(UUID id) {
        table.deleteItem(Key.builder().partitionValue(id.toString()).build());
    }
}
