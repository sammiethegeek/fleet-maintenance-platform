package com.fleet.maintenance.decision.service;

import com.fleet.maintenance.decision.dto.DecisionRequest;
import com.fleet.maintenance.decision.entity.Decision;
import com.fleet.maintenance.decision.mapper.DecisionMapper;
import com.fleet.maintenance.decision.repository.DecisionRepository;
import com.fleet.maintenance.decision.validation.DecisionValidator;
import com.fleet.maintenance.infra.kafka.PaymentEventPublisher;
import com.fleet.maintenance.request.dto.MaintenanceStatusResponse;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.request.service.RequestService;
import com.fleet.maintenance.shared.dto.DecisionType;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.security.UserPrincipal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DecisionService {
    private static final Logger log = LoggerFactory.getLogger(DecisionService.class);

    private final RequestService requestService;
    private final DecisionRepository decisionRepository;
    private final DecisionMapper decisionMapper;
    private final DecisionValidator decisionValidator;
    private final PaymentEventPublisher paymentEventPublisher;

    public DecisionService(
            RequestService requestService,
            DecisionRepository decisionRepository,
            DecisionMapper decisionMapper,
            DecisionValidator decisionValidator,
            PaymentEventPublisher paymentEventPublisher
    ) {
        this.requestService = requestService;
        this.decisionRepository = decisionRepository;
        this.decisionMapper = decisionMapper;
        this.decisionValidator = decisionValidator;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public MaintenanceStatusResponse decide(UUID id, DecisionRequest dto, UserPrincipal principal) {
        MaintenanceRequest request = requestService.findEntity(id);
        decisionValidator.validateDecision(request, principal);
        Decision decision = decisionMapper.toEntity(dto);
        decision.setMaintenanceId(request.getMaintenanceId());
        decision.setDecidedBy(principal.id());
        decisionRepository.save(decision);

        MaintenanceStatus status = toStatus(dto.decisionType());
        request.setStatus(status);
        request.setUpdatedOn(dto.updatedOn());
        requestService.save(request);
        log.info("Maintenance request {} decision {} by {}", id, dto.decisionType(), principal.id());

        if (dto.decisionType() == DecisionType.APPROVE) {
            paymentEventPublisher.publishPaymentInitiatedEvent(request.getMaintenanceId(), request.getRequesterId());
        }
        return new MaintenanceStatusResponse(request.getMaintenanceId(), request.getStatus());
    }

    private MaintenanceStatus toStatus(DecisionType decisionType) {
        return switch (decisionType) {
            case APPROVE -> MaintenanceStatus.APPROVED;
            case REJECT -> MaintenanceStatus.REJECTED;
            case REQUEST_MORE_INFO -> MaintenanceStatus.RFI_REQUESTED;
        };
    }
}
