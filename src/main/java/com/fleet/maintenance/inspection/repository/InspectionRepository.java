package com.fleet.maintenance.inspection.repository;

import com.fleet.maintenance.inspection.entity.InspectionReport;
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
public class InspectionRepository {
    private final DynamoDbTable<InspectionReport> table;

    public InspectionRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("inspection_reports", TableSchema.fromBean(InspectionReport.class));
    }

    public InspectionReport save(InspectionReport report) {
        if (report.getCreatedAt() == null) {
            report.touchForCreate();
        } else {
            report.touchForUpdate();
        }
        table.putItem(report);
        return report;
    }

    public Optional<InspectionReport> findById(UUID id) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id.toString()).build()));
    }

    public List<InspectionReport> findAll() {
        return table.scan().items().stream().toList();
    }

    public Optional<InspectionReport> findTopByMaintenanceRequestMaintenanceIdOrderByCreatedAtDesc(UUID maintenanceId) {
        return findAll().stream()
                .filter(report -> maintenanceId.equals(report.getMaintenanceId()))
                .max(Comparator.comparing(InspectionReport::getCreatedAt));
    }

    public void delete(UUID id) {
        table.deleteItem(Key.builder().partitionValue(id.toString()).build());
    }
}
