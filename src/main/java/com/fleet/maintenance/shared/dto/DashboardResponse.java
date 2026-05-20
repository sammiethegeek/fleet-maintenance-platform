package com.fleet.maintenance.shared.dto;

import java.util.List;

public record DashboardResponse(int totalCount, List<DashboardItem> items) {
}
