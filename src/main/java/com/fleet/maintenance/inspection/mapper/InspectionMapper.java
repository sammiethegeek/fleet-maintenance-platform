package com.fleet.maintenance.inspection.mapper;

import com.fleet.maintenance.inspection.dto.InspectionReportRequest;
import com.fleet.maintenance.inspection.entity.InspectionReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InspectionMapper {
    @Mapping(target = "inspectionId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    InspectionReport toEntity(InspectionReportRequest request);
}
