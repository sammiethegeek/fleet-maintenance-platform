package com.fleet.maintenance.inspection.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fleet.maintenance.inspection.service.InspectionService;
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

@WebMvcTest(InspectionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InspectionControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    InspectionService inspectionService;
    @MockBean
    JwtTokenUtil jwtTokenUtil;

    @Test
    void should_ReturnOk_When_InspectionReportIsValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(inspectionService.submitInspection(any(), any(), any()))
                .thenReturn(new MaintenanceStatusResponse(id, MaintenanceStatus.PENDING_APPROVAL));

        mockMvc.perform(post("/maintenance-requests/{id}/inspection", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload(id, 100.0, "Findings")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    void should_ReturnBadRequest_When_FindingsAreNull() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/maintenance-requests/{id}/inspection", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload(id, 100.0, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void should_ReturnBadRequest_When_EstimatedCostIsNegative() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/maintenance-requests/{id}/inspection", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload(id, -1.0, "Findings")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void should_ReturnBadRequest_When_ReportAlreadySubmitted() throws Exception {
        UUID id = UUID.randomUUID();
        when(inspectionService.submitInspection(any(), any(), any()))
                .thenThrow(new BadRequestException("Invalid transition: inspection submission requires ASSIGNED status"));

        mockMvc.perform(post("/maintenance-requests/{id}/inspection", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload(id, 100.0, "Findings")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid transition: inspection submission requires ASSIGNED status"));
    }

    @Test
    void should_ReturnNotFound_When_RequestDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(inspectionService.submitInspection(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Maintenance request not found: " + id));

        mockMvc.perform(post("/maintenance-requests/{id}/inspection", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload(id, 100.0, "Findings")))
                .andExpect(status().isNotFound());
    }

    private String validPayload(UUID id, double cost, String findings) {
        String findingsJson = findings == null ? "null" : "\"" + findings + "\"";
        return """
                {"maintenanceId":"%s","updatedOn":"2026-05-20T11:00:00","inspectionReport":%s,"estimatedCost":%s,"inspectedOn":"2026-05-20T10:30:00","estimatedCompletionDate":"2026-05-21T10:00:00","additionalDetails":"Parts available"}
                """.formatted(id, findingsJson, cost);
    }
}
