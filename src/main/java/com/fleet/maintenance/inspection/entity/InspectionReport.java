package com.fleet.maintenance.inspection.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@DynamoDbBean
public class InspectionReport {
    private UUID id;
    private UUID maintenanceId;
    private String inspectionReport;
    private Double estimatedCost;
    private LocalDateTime inspectedOn;
    private LocalDateTime estimatedCompletionDate;
    private String additionalDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public UUID getId() {
        return id;
    }

    @DynamoDbAttribute("maintenanceId")
    public UUID getMaintenanceId() {
        return maintenanceId;
    }

    @DynamoDbAttribute("inspectionReport")
    public String getInspectionReport() {
        return inspectionReport;
    }

    @DynamoDbAttribute("estimatedCost")
    public Double getEstimatedCost() {
        return estimatedCost;
    }

    @DynamoDbAttribute("inspectedOn")
    public LocalDateTime getInspectedOn() {
        return inspectedOn;
    }

    @DynamoDbAttribute("estimatedCompletionDate")
    public LocalDateTime getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    @DynamoDbAttribute("additionalDetails")
    public String getAdditionalDetails() {
        return additionalDetails;
    }

    @DynamoDbAttribute("createdAt")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @DynamoDbIgnore
    public UUID getInspectionId() {
        return id;
    }

    public void setInspectionId(UUID inspectionId) {
        this.id = inspectionId;
    }

    public void touchForCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    public void touchForUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
