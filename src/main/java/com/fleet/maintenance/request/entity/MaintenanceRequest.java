package com.fleet.maintenance.request.entity;

import com.fleet.maintenance.shared.dto.MaintenanceStatus;
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
public class MaintenanceRequest {
    private UUID id;
    private String vehicleId;
    private MaintenanceStatus status;
    private String requesterId;
    private String requesterName;
    private String description;
    private String severity;
    private String impact;
    private Integer impactedPeopleCount;
    private String assignedTo;
    private String providerName;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public UUID getId() {
        return id;
    }

    @DynamoDbAttribute("vehicleId")
    public String getVehicleId() {
        return vehicleId;
    }

    @DynamoDbAttribute("status")
    public MaintenanceStatus getStatus() {
        return status;
    }

    @DynamoDbAttribute("requesterId")
    public String getRequesterId() {
        return requesterId;
    }

    @DynamoDbAttribute("requesterName")
    public String getRequesterName() {
        return requesterName;
    }

    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    @DynamoDbAttribute("severity")
    public String getSeverity() {
        return severity;
    }

    @DynamoDbAttribute("impact")
    public String getImpact() {
        return impact;
    }

    @DynamoDbAttribute("impactedPeopleCount")
    public Integer getImpactedPeopleCount() {
        return impactedPeopleCount;
    }

    @DynamoDbAttribute("assignedTo")
    public String getAssignedTo() {
        return assignedTo;
    }

    @DynamoDbAttribute("providerName")
    public String getProviderName() {
        return providerName;
    }

    @DynamoDbAttribute("createdOn")
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    @DynamoDbAttribute("updatedOn")
    public LocalDateTime getUpdatedOn() {
        return updatedOn;
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
    public UUID getMaintenanceId() {
        return id;
    }

    public void setMaintenanceId(UUID maintenanceId) {
        this.id = maintenanceId;
    }

    public void touchForCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = MaintenanceStatus.CREATED;
        }
        if (createdOn == null) {
            createdOn = now;
        }
        if (updatedOn == null) {
            updatedOn = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    public void touchForUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
