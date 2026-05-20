package com.fleet.maintenance.shared.dynamodb;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Component
public class DynamoDbTableInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbTableInitializer.class);
    private static final List<TableSpec> TABLES = List.of(
            new TableSpec("users", "username"),
            new TableSpec("maintenance_requests", "id"),
            new TableSpec("inspection_reports", "id"),
            new TableSpec("decisions", "id")
    );

    private final DynamoDbClient dynamoDbClient;

    public DynamoDbTableInitializer(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public void run(String... args) {
        TABLES.forEach(this::createIfMissing);
    }

    private void createIfMissing(TableSpec table) {
        try {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(table.tableName())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName(table.partitionKey())
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .keySchema(KeySchemaElement.builder()
                            .attributeName(table.partitionKey())
                            .keyType(KeyType.HASH)
                            .build())
                    .build());
            log.info("DynamoDB table {} creation requested", table.tableName());
        } catch (ResourceInUseException ignored) {
            log.debug("DynamoDB table {} already exists", table.tableName());
        }
    }

    private record TableSpec(String tableName, String partitionKey) {
    }
}
