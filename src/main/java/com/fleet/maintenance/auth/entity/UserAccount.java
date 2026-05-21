package com.fleet.maintenance.auth.entity;

import com.fleet.maintenance.shared.dto.Role;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@DynamoDbBean
public class UserAccount {
    private String username;
    private String name;
    private String passwordHash;
    private Role role;
    private Instant createdAt;
    private Instant updatedAt;
    private String userId;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("username")
    public String getUsername() {
        return username;
    }

    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    @DynamoDbAttribute("passwordHash")
    public String getPasswordHash() {
        return passwordHash;
    }

    @DynamoDbAttribute("role")
    public Role getRole() {
        return role;
    }

    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void touchForCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    public void touchForUpdate() {
        updatedAt = Instant.now();
    }
}
