package com.fleet.maintenance.shared.validation;

import static com.fleet.maintenance.TestFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.fleet.maintenance.auth.dto.LoginRequest;
import com.fleet.maintenance.decision.dto.DecisionRequest;
import com.fleet.maintenance.inspection.dto.InspectionReportRequest;
import com.fleet.maintenance.request.dto.CreateRequestRequest;
import com.fleet.maintenance.shared.dto.DecisionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DtoValidationTest {
    Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void should_RejectBlankUsernameAndPassword_When_LoginRequestIsValidated() {
        var violations = validator.validate(new LoginRequest("", ""));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("username", "password");
    }

    @Test
    void should_RejectTooLongUsername_When_LoginRequestIsValidated() {
        var violations = validator.validate(new LoginRequest("a".repeat(101), "password"));

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void should_RejectNullMandatoryFields_When_CreateRequestIsValidated() {
        var violations = validator.validate(new CreateRequestRequest(null, null, null, null, null, null));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("vehicleId", "description", "severity", "impact", "impactedPeopleCount", "createdOn");
    }

    @Test
    void should_RejectNegativeImpactedPeopleCount_When_CreateRequestIsValidated() {
        var violations = validator.validate(new CreateRequestRequest("VH-1", "Broken", "HIGH", "Route down", -1, NOW));

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("impactedPeopleCount"));
    }

    @Test
    void should_RejectNullFindings_When_InspectionRequestIsValidated() {
        UUID id = UUID.randomUUID();
        var violations = validator.validate(new InspectionReportRequest(id, NOW, null, 1.0, NOW, NOW.plusDays(1), null));

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("inspectionReport"));
    }

    @Test
    void should_RejectNegativeEstimatedCost_When_InspectionRequestIsValidated() {
        UUID id = UUID.randomUUID();
        var violations = validator.validate(new InspectionReportRequest(id, NOW, "Findings", -0.01, NOW, NOW.plusDays(1), null));

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("estimatedCost"));
    }

    @Test
    void should_AcceptZeroAndLargeEstimatedCosts_When_InspectionRequestIsValidated() {
        UUID id = UUID.randomUUID();

        assertThat(validator.validate(new InspectionReportRequest(id, NOW, "Findings", 0.0, NOW, NOW.plusDays(1), null))).isEmpty();
        assertThat(validator.validate(new InspectionReportRequest(id, NOW, "Findings", 999_999_999.99, NOW, NOW.plusDays(1), null))).isEmpty();
    }

    @Test
    void should_RejectNullDecisionTypeAndBlankRemarks_When_DecisionRequestIsValidated() {
        UUID id = UUID.randomUUID();
        var violations = validator.validate(new DecisionRequest(id, null, "", NOW));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("decisionType", "remarks");
    }

    @Test
    void should_AcceptApprovalRemarks_When_DecisionRequestIsValidated() {
        var violations = validator.validate(new DecisionRequest(UUID.randomUUID(), DecisionType.APPROVE, "Approved", NOW));

        assertThat(violations).isEmpty();
    }
}
