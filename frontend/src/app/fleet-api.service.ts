import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type MaintenanceStatus = 'CREATED' | 'ASSIGNED' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'RFI_REQUESTED' | 'PAYMENT_INITIATED';
export type DecisionType = 'APPROVE' | 'REJECT' | 'REQUEST_MORE_INFO';

export interface DashboardResponse {
  totalCount: number;
  role: string;
  items: DashboardItem[];
}

export interface DashboardItem {
  request: DashboardRequest;
  inspection: DashboardInspection | null;
}

export interface DashboardRequest {
  maintenanceId: string;
  vehicleId: string;
  status: MaintenanceStatus;
  description: string;
  createdOn: string;
  requesterName?: string;
}

export interface DashboardInspection {
  inspectionId: string;
  estimatedAmount: number;
  inspectionDate: string;
}

export interface MaintenanceDetails {
  maintenanceId: string;
  status: MaintenanceStatus;
  vehicleId: string;
  requesterId: string;
  description: string;
  severity: string;
  impact: string;
  impactedPeopleCount: number;
  createdOn: string;
  updatedOn: string;
  assignedTo?: string;
  estimatedCost?: number;
  inspectionReport?: string;
  inspectedOn?: string;
  estimatedCompletionDate?: string;
  additionalDetails?: string;
}

@Injectable({ providedIn: 'root' })
export class FleetApiService {
  private readonly apiBase = 'http://localhost:3000/api';

  constructor(private readonly http: HttpClient) {
  }

  dashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.apiBase}/dashboard`);
  }

  details(id: string): Observable<MaintenanceDetails> {
    return this.http.get<MaintenanceDetails>(`${this.apiBase}/maintenance-requests/${id}`);
  }

  createRequest(payload: {
    vehicleId: string;
    description: string;
    severity: string;
    impact: string;
    impactedPeopleCount: number;
  }): Observable<unknown> {
    return this.http.post(`${this.apiBase}/maintenance-requests`, payload);
  }

  assignProvider(id: string, payload: { providerId: string; providerName: string }): Observable<unknown> {
    return this.http.put(`${this.apiBase}/maintenance-requests/${id}/assign-provider`, {
      providerId: payload.providerId,
      providerName: payload.providerName
    });
  }

  submitInspection(id: string, payload: {
    findings: string;
    estimatedCost: number;
    estimatedTime: string;
    inspectionDate: string;
  }): Observable<unknown> {
    return this.http.post(`${this.apiBase}/maintenance-requests/${id}/inspection`, payload);
  }

  submitDecision(id: string, decisionType: DecisionType, remarks: string): Observable<unknown> {
    return this.http.post(`${this.apiBase}/maintenance-requests/${id}/decision`, { decisionType, remarks });
  }
}
