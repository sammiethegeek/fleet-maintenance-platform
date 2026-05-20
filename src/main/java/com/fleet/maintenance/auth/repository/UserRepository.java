package com.fleet.maintenance.auth.repository;

import com.fleet.maintenance.auth.entity.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class UserRepository {
    private final DynamoDbTable<UserAccount> table;

    public UserRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.table = dynamoDbEnhancedClient.table("users", TableSchema.fromBean(UserAccount.class));
    }

    public UserAccount save(UserAccount user) {
        if (user.getCreatedAt() == null) {
            user.touchForCreate();
        } else {
            user.touchForUpdate();
        }
        table.putItem(user);
        return user;
    }

    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(username).build()));
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public List<UserAccount> findAll() {
        return table.scan().items().stream().toList();
    }

    public void delete(String username) {
        table.deleteItem(Key.builder().partitionValue(username).build());
    }
}
