package com.fleet.maintenance.shared.service;

import com.fleet.maintenance.inspection.entity.InspectionReport;
import com.fleet.maintenance.inspection.repository.InspectionRepository;
import com.fleet.maintenance.request.entity.MaintenanceRequest;
import com.fleet.maintenance.request.mapper.RequestMapper;
import com.fleet.maintenance.request.repository.RequestRepository;
import com.fleet.maintenance.shared.dto.DashboardItem;
import com.fleet.maintenance.shared.dto.DashboardResponse;
import com.fleet.maintenance.shared.dto.Role;
import com.fleet.maintenance.shared.security.UserPrincipal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final RequestRepository requestRepository;
    private final InspectionRepository inspectionRepository;
    private final RequestMapper requestMapper;

    public DashboardService(
            RequestRepository requestRepository,
            InspectionRepository inspectionRepository,
            RequestMapper requestMapper
    ) {
        this.requestRepository = requestRepository;
        this.inspectionRepository = inspectionRepository;
        this.requestMapper = requestMapper;
    }

    public DashboardResponse getDashboard(UserPrincipal principal) {
        List<MaintenanceRequest> requests = principal.role() == Role.ROLE_COORDINATOR
                ? requestRepository.findByRequesterId(principal.id())
                : requestRepository.findByAssignedTo(principal.id());
        List<DashboardItem> items = requests.stream()
                .map(request -> {
                    InspectionReport inspection = inspectionRepository
                            .findTopByMaintenanceRequestMaintenanceIdOrderByCreatedAtDesc(request.getMaintenanceId())
                            .orElse(null);
                    return requestMapper.toDashboardItem(request, inspection);
                })
                .toList();
        return new DashboardResponse(items.size(), items);
    }
}
