package com.fleet.maintenance.infra.kafka;

import com.fleet.maintenance.shared.dto.EventType;
import com.fleet.maintenance.shared.dto.KafkaEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private static final String TOPIC = "payment-readiness-topic";

    private final KafkaTemplate<String, KafkaEvent> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, KafkaEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentInitiatedEvent(UUID maintenanceId, String requesterId) {
        KafkaEvent event = new KafkaEvent(maintenanceId, requesterId, EventType.PAYMENT_INITIATED, LocalDateTime.now());
        kafkaTemplate.send(TOPIC, maintenanceId.toString(), event);
        log.info("Payment initiated event published for maintenanceId: {}", maintenanceId);
    }
}
