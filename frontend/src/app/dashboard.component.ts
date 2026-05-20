import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { DashboardItem, FleetApiService, MaintenanceDetails, MaintenanceStatus } from './fleet-api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <main class="min-h-screen bg-[linear-gradient(180deg,_#eff6ff_0%,_#f8fafc_26%,_#f8fafc_100%)] px-4 py-6">
      <section class="mx-auto max-w-6xl">
        <header class="rounded-[28px] border border-white/70 bg-white/90 p-6 shadow-lg shadow-blue-100/60 backdrop-blur">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.22em] text-blue-600">Smart Fleet Maintenance</p>
              <h1 class="mt-2 text-3xl font-semibold text-slate-900">Maintenance Dashboard</h1>
              <p class="mt-1 text-sm text-slate-600">{{ auth.username() }} - {{ role }}</p>
            </div>
            <div class="flex flex-wrap gap-2">
              <a
                *ngIf="role === 'COORDINATOR'"
                routerLink="/request-form"
                class="rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2"
              >
                Create Request
              </a>
              <button
                type="button"
                (click)="logout()"
                class="rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2"
              >
                Logout
              </button>
            </div>
          </div>

          <div class="mt-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div class="text-sm text-slate-600">Open any row to inspect details and continue the workflow.</div>
            <div class="flex items-center gap-3">
              <label for="status" class="text-sm font-medium text-slate-700">Status</label>
              <select
                id="status"
                [value]="statusFilter"
                (change)="statusFilter = $any($event.target).value"
                class="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-100"
              >
                <option value="">All</option>
                <option value="CREATED">Created</option>
                <option value="ASSIGNED">Assigned</option>
                <option value="PENDING_APPROVAL">Pending approval</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
                <option value="RFI_REQUESTED">More info requested</option>
              </select>
            </div>
          </div>
        </header>

        <p *ngIf="error" class="mt-4 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">{{ error }}</p>

        <div class="mt-5 overflow-hidden rounded-[28px] border border-white/70 bg-white/95 shadow-lg shadow-slate-200/70">
          <table class="min-w-full divide-y divide-slate-200">
            <thead class="bg-slate-50/90">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">ID</th>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Vehicle</th>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Status</th>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Description</th>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Created</th>
                <th *ngIf="role === 'PROVIDER'" class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-600">Requester</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                *ngFor="let item of filteredItems()"
                tabindex="0"
                (click)="openDetails(item)"
                (keydown.enter)="openDetails(item)"
                class="cursor-pointer bg-white transition hover:bg-blue-50/70 focus:bg-blue-50/70 focus:outline-none"
              >
                <td class="max-w-40 truncate px-4 py-3 text-sm font-mono text-slate-700">{{ item.request.maintenanceId }}</td>
                <td class="px-4 py-3 text-sm">{{ item.request.vehicleId }}</td>
                <td class="px-4 py-3 text-sm"><span [class]="badgeClass(item.request.status)">{{ item.request.status }}</span></td>
                <td class="max-w-xs truncate px-4 py-3 text-sm">{{ item.request.description }}</td>
                <td class="px-4 py-3 text-sm">{{ item.request.createdOn | date:'short' }}</td>
                <td *ngIf="role === 'PROVIDER'" class="px-4 py-3 text-sm">{{ item.request.requesterName || '-' }}</td>
              </tr>
              <tr *ngIf="!loading && filteredItems().length === 0">
                <td [attr.colspan]="role === 'PROVIDER' ? 6 : 5" class="px-4 py-10 text-center text-sm text-slate-500">No requests found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <div *ngIf="selected" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 px-4" role="dialog" aria-modal="true" aria-labelledby="details-title">
        <section class="w-full max-w-3xl rounded-[28px] bg-white p-6 shadow-2xl">
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs font-semibold uppercase tracking-[0.22em] text-blue-600">Workflow Details</p>
              <h2 id="details-title" class="mt-2 text-xl font-semibold">Request Details</h2>
              <p class="mt-1 text-sm text-slate-600">{{ selected.maintenanceId }}</p>
            </div>
            <button type="button" (click)="selected = null" class="rounded-xl px-3 py-2 text-sm text-slate-600 hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-600">Close</button>
          </div>

          <dl class="mt-5 grid gap-3 rounded-2xl bg-slate-50 p-4 sm:grid-cols-2">
            <div><dt class="text-xs font-semibold uppercase text-slate-500">Vehicle</dt><dd class="mt-1 text-sm">{{ selected.vehicleId }}</dd></div>
            <div><dt class="text-xs font-semibold uppercase text-slate-500">Status</dt><dd class="mt-1"><span [class]="badgeClass(selected.status)">{{ selected.status }}</span></dd></div>
            <div><dt class="text-xs font-semibold uppercase text-slate-500">Assigned To</dt><dd class="mt-1 text-sm">{{ selected.assignedTo || '-' }}</dd></div>
            <div><dt class="text-xs font-semibold uppercase text-slate-500">Estimated Cost</dt><dd class="mt-1 text-sm">{{ selected.estimatedCost || '-' }}</dd></div>
            <div class="sm:col-span-2"><dt class="text-xs font-semibold uppercase text-slate-500">Description</dt><dd class="mt-1 text-sm leading-6">{{ selected.description }}</dd></div>
            <div class="sm:col-span-2"><dt class="text-xs font-semibold uppercase text-slate-500">Inspection</dt><dd class="mt-1 text-sm leading-6">{{ selected.inspectionReport || 'Not submitted yet' }}</dd></div>
          </dl>

          <form
            *ngIf="role === 'COORDINATOR' && selected.status === 'CREATED'"
            class="mt-5 space-y-4 rounded-2xl border border-slate-200 p-4"
            (ngSubmit)="assignProvider(selected.maintenanceId)"
            #assignForm="ngForm"
          >
            <div class="flex items-center justify-between gap-4">
              <div>
                <h3 class="text-base font-semibold text-slate-900">Assign Provider</h3>
                <p class="text-sm text-slate-600">Choose the provider account that will inspect this request.</p>
              </div>
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <div>
                <label for="providerName" class="block text-sm font-semibold text-slate-800">
                  Provider Name <span class="text-red-500">*</span>
                </label>
                <input
                  id="providerName"
                  name="providerName"
                  #providerNameModel="ngModel"
                  [(ngModel)]="assignDraft.providerName"
                  required
                  class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
                  [class.border-red-300]="providerNameModel.invalid && providerNameModel.touched"
                  [class.border-slate-300]="!(providerNameModel.invalid && providerNameModel.touched)"
                />
                <p *ngIf="providerNameModel.invalid && providerNameModel.touched" class="mt-1 text-xs text-red-600">Provider name is required.</p>
              </div>

              <div>
                <label for="providerId" class="block text-sm font-semibold text-slate-800">
                  Provider ID <span class="text-red-500">*</span>
                </label>
                <input
                  id="providerId"
                  name="providerId"
                  #providerIdModel="ngModel"
                  [(ngModel)]="assignDraft.providerId"
                  required
                  class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
                  [class.border-red-300]="providerIdModel.invalid && providerIdModel.touched"
                  [class.border-slate-300]="!(providerIdModel.invalid && providerIdModel.touched)"
                />
                <p *ngIf="providerIdModel.invalid && providerIdModel.touched" class="mt-1 text-xs text-red-600">Provider ID is required.</p>
              </div>
            </div>

            <button
              type="submit"
              [disabled]="assignForm.invalid || loading"
              class="rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 disabled:opacity-60"
            >
              Assign Provider
            </button>
          </form>

          <div class="mt-5 flex flex-wrap gap-2">
            <a
            *ngIf="role === 'PROVIDER' && selected.status === 'ASSIGNED'"
            [routerLink]="['/inspection-form', selected.maintenanceId]"
            class="rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2"
          >
            Submit Inspection
          </a>
            <a
              *ngIf="role === 'COORDINATOR' && selected.status === 'PENDING_APPROVAL'"
              [routerLink]="['/decision-form', selected.maintenanceId]"
              class="rounded-xl bg-green-600 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-green-200 transition hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-600 focus:ring-offset-2"
            >
              Approve/Reject
            </a>
          </div>

          <p
            *ngIf="isReadOnlyState()"
            class="mt-5 rounded-2xl bg-slate-50 px-4 py-3 text-sm text-slate-600"
          >
            This ticket is read-only in the current workflow state.
          </p>
        </section>
      </div>
    </main>
  `
})
export class DashboardComponent implements OnInit {
  role = this.auth.role();
  items: DashboardItem[] = [];
  selected: MaintenanceDetails | null = null;
  detailsCache: Record<string, MaintenanceDetails> = {};
  statusFilter = '';
  loading = true;
  error = '';
  assignDraft = {
    providerName: 'Provider',
    providerId: 'provider'
  };

  constructor(
    readonly auth: AuthService,
    private readonly api: FleetApiService,
    private readonly router: Router
  ) {
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api.dashboard().subscribe({
      next: (response) => {
        this.items = response.items ?? [];
        this.syncDetailsCacheWithDashboard();
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load dashboard. Confirm the BFF and backend are running.';
        this.loading = false;
      }
    });
  }

  filteredItems(): DashboardItem[] {
    return this.statusFilter
      ? this.items.filter((item) => item.request.status === this.statusFilter)
      : this.items;
  }

  openDetails(item: DashboardItem): void {
    const cached = this.detailsCache[item.request.maintenanceId];
    if (cached && cached.status === item.request.status) {
      this.assignDraft = { providerName: 'Provider', providerId: 'provider' };
      this.selected = cached;
      return;
    }

    if (cached && cached.status !== item.request.status) {
      delete this.detailsCache[item.request.maintenanceId];
    }

    this.api.details(item.request.maintenanceId).subscribe({
      next: (details) => {
        this.detailsCache[item.request.maintenanceId] = details;
        this.assignDraft = { providerName: 'Provider', providerId: 'provider' };
        this.selected = details;
      },
      error: () => this.error = 'Unable to load request details.'
    });
  }

  assignProvider(id: string): void {
    this.api.assignProvider(id, {
      providerId: this.assignDraft.providerId,
      providerName: this.assignDraft.providerName
    }).subscribe({
      next: () => {
        delete this.detailsCache[id];
        this.selected = null;
        this.load();
      },
      error: () => this.error = 'Unable to assign provider.'
    });
  }

  logout(): void {
    this.clearViewState();
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  badgeClass(status: MaintenanceStatus): string {
    const base = 'inline-flex rounded-full px-2 py-1 text-xs font-semibold text-white';
    const colors: Record<MaintenanceStatus, string> = {
      CREATED: 'bg-gray-500',
      ASSIGNED: 'bg-blue-500',
      PENDING_APPROVAL: 'bg-yellow-500 text-slate-950',
      APPROVED: 'bg-green-500',
      REJECTED: 'bg-red-500',
      RFI_REQUESTED: 'bg-yellow-500 text-slate-950',
      PAYMENT_INITIATED: 'bg-green-500'
    };
    return `${base} ${colors[status]}`;
  }

  isReadOnlyState(): boolean {
    if (!this.selected) {
      return false;
    }

    if (this.role === 'PROVIDER') {
      return this.selected.status !== 'ASSIGNED';
    }

    if (this.role === 'COORDINATOR') {
      return this.selected.status === 'ASSIGNED' || (this.selected.status !== 'CREATED' && this.selected.status !== 'PENDING_APPROVAL');
    }

    return true;
  }

  private syncDetailsCacheWithDashboard(): void {
    const currentStatuses = new Map(this.items.map((item) => [item.request.maintenanceId, item.request.status]));

    Object.keys(this.detailsCache).forEach((maintenanceId) => {
      const dashboardStatus = currentStatuses.get(maintenanceId);
      if (!dashboardStatus || this.detailsCache[maintenanceId].status !== dashboardStatus) {
        delete this.detailsCache[maintenanceId];
      }
    });
  }

  private clearViewState(): void {
    this.items = [];
    this.selected = null;
    this.detailsCache = {};
    this.statusFilter = '';
    this.loading = false;
    this.error = '';
    this.assignDraft = {
      providerName: 'Provider',
      providerId: 'provider'
    };
  }
}
