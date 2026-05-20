package com.fleet.maintenance.request.mapper;

import com.fleet.maintenance.inspection.entity.InspectionReport;
import com.fleet.maintenance.request.dto.CreateRequestRequest;
import com.fleet.maintenance.request.dto.MaintenanceRequestResponse;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.shared.dto.DashboardInspection;
import com.fleet.maintenance.shared.dto.DashboardItem;
import com.fleet.maintenance.shared.dto.DashboardRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    @Mapping(target = "maintenanceId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "requesterId", ignore = true)
    @Mapping(target = "requesterName", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "providerName", ignore = true)
    @Mapping(target = "updatedOn", source = "createdOn")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MaintenanceRequest toEntity(CreateRequestRequest request);

    @Mapping(target = "estimatedCost", source = "inspection.estimatedCost")
    @Mapping(target = "maintenanceId", source = "request.maintenanceId")
    @Mapping(target = "inspectionReport", source = "inspection.inspectionReport")
    @Mapping(target = "inspectedOn", source = "inspection.inspectedOn")
    @Mapping(target = "estimatedCompletionDate", source = "inspection.estimatedCompletionDate")
    @Mapping(target = "additionalDetails", source = "inspection.additionalDetails")
    MaintenanceRequestResponse toResponse(MaintenanceRequest request, InspectionReport inspection);

    default DashboardItem toDashboardItem(MaintenanceRequest request, InspectionReport inspection) {
        DashboardRequest dashboardRequest = new DashboardRequest(
                request.getMaintenanceId(),
                request.getVehicleId(),
                request.getStatus(),
                request.getDescription(),
                request.getCreatedOn(),
                request.getRequesterName()
        );
        DashboardInspection dashboardInspection = inspection == null ? null : new DashboardInspection(
                inspection.getInspectionId(),
                inspection.getEstimatedCost(),
                inspection.getInspectedOn()
        );
        return new DashboardItem(dashboardRequest, dashboardInspection);
    }
}
