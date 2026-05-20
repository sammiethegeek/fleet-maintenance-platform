package com.fleet.maintenance.decision.mapper;

import com.fleet.maintenance.decision.dto.DecisionRequest;
import com.fleet.maintenance.decision.entity.Decision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DecisionMapper {
    @Mapping(target = "decisionId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decidedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Decision toEntity(DecisionRequest request);
}
