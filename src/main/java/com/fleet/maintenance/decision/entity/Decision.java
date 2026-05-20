package com.fleet.maintenance.decision.entity;

import com.fleet.maintenance.shared.dto.DecisionType;
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
public class Decision {
    private UUID id;
    private UUID maintenanceId;
    private DecisionType decisionType;
    private String remarks;
    private String decidedBy;
    private LocalDateTime updatedOn;
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

    @DynamoDbAttribute("decisionType")
    public DecisionType getDecisionType() {
        return decisionType;
    }

    @DynamoDbAttribute("remarks")
    public String getRemarks() {
        return remarks;
    }

    @DynamoDbAttribute("decidedBy")
    public String getDecidedBy() {
        return decidedBy;
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
    public UUID getDecisionId() {
        return id;
    }

    public void setDecisionId(UUID decisionId) {
        this.id = decisionId;
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
