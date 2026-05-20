package com.fleet.maintenance.request.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fleet.maintenance.request.dto.MaintenanceRequestResponse;
import com.fleet.maintenance.request.dto.MaintenanceStatusResponse;
import com.fleet.maintenance.request.service.RequestService;
import com.fleet.maintenance.shared.dto.DashboardResponse;
import com.fleet.maintenance.shared.dto.MaintenanceStatus;
import com.fleet.maintenance.shared.exception.BadRequestException;
import com.fleet.maintenance.shared.exception.ResourceNotFoundException;
import com.fleet.maintenance.shared.security.JwtTokenUtil;
import com.fleet.maintenance.shared.service.DashboardService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class RequestControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    RequestService requestService;
    @MockBean
    DashboardService dashboardService;
    @MockBean
    JwtTokenUtil jwtTokenUtil;

    @Test
    void should_ReturnCreated_When_CreateRequestIsValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.create(any(), any())).thenReturn(new MaintenanceStatusResponse(id, MaintenanceStatus.CREATED));

        mockMvc.perform(post("/maintenance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId":"VH-1","description":"Broken","severity":"HIGH","impact":"Route down","impactedPeopleCount":1,"createdOn":"2026-05-20T10:00:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void should_ReturnBadRequest_When_CreateRequestHasInvalidMandatoryFields() throws Exception {
        mockMvc.perform(post("/maintenance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicleId":"","description":"","severity":"","impact":"","impactedPeopleCount":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void should_ReturnOk_When_AssignProviderIsValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.assignProvider(any(), any(), any())).thenReturn(new MaintenanceStatusResponse(id, MaintenanceStatus.ASSIGNED));

        mockMvc.perform(put("/maintenance-requests/{id}/assign-provider", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"maintenanceId":"%s","providerName":"Provider","providerId":"provider","updatedOn":"2026-05-20T10:05:00"}
                                """.formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void should_ReturnBadRequest_When_AssignProviderTransitionIsInvalid() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.assignProvider(any(), any(), any()))
                .thenThrow(new BadRequestException("Invalid transition: provider assignment requires CREATED status"));

        mockMvc.perform(put("/maintenance-requests/{id}/assign-provider", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"maintenanceId":"%s","providerName":"Provider","providerId":"provider","updatedOn":"2026-05-20T10:05:00"}
                                """.formatted(id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid transition: provider assignment requires CREATED status"));
    }

    @Test
    void should_ReturnNotFound_When_RequestDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.getDetails(any(), any())).thenThrow(new ResourceNotFoundException("Maintenance request not found: " + id));

        mockMvc.perform(get("/maintenance-requests/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Maintenance request not found: " + id));
    }

    @Test
    void should_ReturnDetails_When_RequestExists() throws Exception {
        UUID id = UUID.randomUUID();
        when(requestService.getDetails(any(), any())).thenReturn(new MaintenanceRequestResponse(
                id, MaintenanceStatus.CREATED, "VH-1", "coordinator", "Broken", "HIGH",
                "Route down", 1, null, null, null, null, null, null, null, null
        ));

        mockMvc.perform(get("/maintenance-requests/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maintenanceId").value(id.toString()));
    }

    @Test
    void should_ReturnDashboard_When_DashboardRequested() throws Exception {
        when(dashboardService.getDashboard(any())).thenReturn(new DashboardResponse(0, List.of()));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());
    }
}
