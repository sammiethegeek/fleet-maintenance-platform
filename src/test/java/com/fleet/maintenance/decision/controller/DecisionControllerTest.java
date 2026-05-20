package com.fleet.maintenance.decision.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fleet.maintenance.decision.service.DecisionService;
import com.fleet.maintenance.request.dto.MaintenanceStatusResponse;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.exception.BadRequestException;
import com.fleet.maintenance.shared.exception.ResourceNotFoundException;
import com.fleet.maintenance.shared.security.JwtTokenUtil;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DecisionController.class)
@AutoConfigureMockMvc(addFilters = false)
class DecisionControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    DecisionService decisionService;
    @MockBean
    JwtTokenUtil jwtTokenUtil;

    @Test
    void should_ReturnApproved_When_ApprovalRequestIsValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(decisionService.decide(any(), any(), any()))
                .thenReturn(new MaintenanceStatusResponse(id, MaintenanceStatus.APPROVED));

        mockMvc.perform(post("/maintenance-requests/{id}/decision", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(id, "APPROVE", "Approved")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void should_ReturnRejected_When_RejectionRequestIsValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(decisionService.decide(any(), any(), any()))
                .thenReturn(new MaintenanceStatusResponse(id, MaintenanceStatus.REJECTED));

        mockMvc.perform(post("/maintenance-requests/{id}/decision", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(id, "REJECT", "Rejected with reason")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void should_ReturnBadRequest_When_RejectionCommentsAreNull() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/maintenance-requests/{id}/decision", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(id, "REJECT", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void should_ReturnBadRequest_When_RejectionCommentsAreEmpty() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/maintenance-requests/{id}/decision", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(id, "REJECT", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void should_ReturnNotFound_When_DecisionRequestDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(decisionService.decide(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Maintenance request not found: " + id));

        mockMvc.perform(post("/maintenance-requests/{id}/decision", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(id, "APPROVE", "Approved")))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_ReturnBadRequest_When_RequestHasNoInspection() throws Exception {
        UUID id = UUID.randomUUID();
        when(decisionService.decide(any(), any(), any()))
                .thenThrow(new BadRequestException("Invalid transition: decision requires PENDING_APPROVAL status"));

        mockMvc.perform(post("/maintenance-requests/{id}/decision", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(id, "APPROVE", "Approved")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid transition: decision requires PENDING_APPROVAL status"));
    }

    private String payload(UUID id, String decisionType, String remarks) {
        String remarksJson = remarks == null ? "null" : "\"" + remarks + "\"";
        return """
                {"maintenanceId":"%s","decisionType":"%s","remarks":%s,"updatedOn":"2026-05-20T12:00:00"}
                """.formatted(id, decisionType, remarksJson);
    }
}
