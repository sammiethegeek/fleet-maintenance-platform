import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { DecisionType, FleetApiService, MaintenanceDetails } from './fleet-api.service';

type FormMode = 'create' | 'inspection' | 'decision';

@Component({
  selector: 'app-request-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <main class="min-h-screen bg-[linear-gradient(180deg,_#f0f9ff_0%,_#f8fafc_30%,_#f8fafc_100%)] px-4 py-6">
      <section class="mx-auto max-w-3xl rounded-[28px] border border-white/70 bg-white/95 p-6 shadow-xl shadow-sky-100/60 backdrop-blur">
        <header class="flex items-center justify-between gap-4 border-b border-slate-200 pb-4">
          <div>
            <p class="text-xs font-semibold uppercase tracking-[0.22em] text-blue-600">{{ auth.role() }} - {{ auth.username() }}</p>
            <h1 class="mt-2 text-3xl font-semibold text-slate-900">{{ title() }}</h1>
            <p class="mt-1 text-sm text-slate-600">{{ subtitle() }}</p>
          </div>
          <a routerLink="/dashboard" class="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2">Back</a>
        </header>

        <p *ngIf="error" class="mt-4 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">{{ error }}</p>

        <form *ngIf="mode === 'create'" class="mt-5 space-y-5" (ngSubmit)="submitCreate(createForm)" #createForm="ngForm" novalidate>
          <div>
            <label for="vehicleId" class="block text-sm font-semibold text-slate-800">
              Vehicle Number <span class="text-red-500">*</span>
            </label>
            <input
              id="vehicleId"
              name="vehicleId"
              #vehicleIdModel="ngModel"
              [(ngModel)]="create.vehicleId"
              required
              class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
              [class.border-red-300]="vehicleIdModel.invalid && vehicleIdModel.touched"
              [class.border-slate-300]="!(vehicleIdModel.invalid && vehicleIdModel.touched)"
            />
            <p *ngIf="vehicleIdModel.invalid && vehicleIdModel.touched" class="mt-1 text-xs text-red-600">Vehicle number is required.</p>
          </div>

          <div>
            <label for="description" class="block text-sm font-semibold text-slate-800">
              Issue Description <span class="text-red-500">*</span>
            </label>
            <textarea
              id="description"
              name="description"
              #descriptionModel="ngModel"
              [(ngModel)]="create.description"
              required
              rows="4"
              class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
              [class.border-red-300]="descriptionModel.invalid && descriptionModel.touched"
              [class.border-slate-300]="!(descriptionModel.invalid && descriptionModel.touched)"
            ></textarea>
            <p *ngIf="descriptionModel.invalid && descriptionModel.touched" class="mt-1 text-xs text-red-600">Issue description is required.</p>
          </div>

          <div class="grid gap-5 sm:grid-cols-2">
            <div>
              <label for="severity" class="block text-sm font-semibold text-slate-800">
                Priority <span class="text-red-500">*</span>
              </label>
              <select
                id="severity"
                name="severity"
                #severityModel="ngModel"
                [(ngModel)]="create.severity"
                required
                class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
                [class.border-red-300]="severityModel.invalid && severityModel.touched"
                [class.border-slate-300]="!(severityModel.invalid && severityModel.touched)"
              >
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
                <option value="CRITICAL">CRITICAL</option>
              </select>
              <p *ngIf="severityModel.invalid && severityModel.touched" class="mt-1 text-xs text-red-600">Priority is required.</p>
            </div>

            <div>
              <label for="impactedPeopleCount" class="block text-sm font-semibold text-slate-800">
                Impacted People Count <span class="text-red-500">*</span>
              </label>
              <input
                id="impactedPeopleCount"
                name="impactedPeopleCount"
                type="number"
                min="0"
                #peopleModel="ngModel"
                [(ngModel)]="create.impactedPeopleCount"
                required
                class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
                [class.border-red-300]="(peopleModel.invalid || create.impactedPeopleCount < 0) && peopleModel.touched"
                [class.border-slate-300]="!((peopleModel.invalid || create.impactedPeopleCount < 0) && peopleModel.touched)"
              />
              <p *ngIf="peopleModel.invalid && peopleModel.touched" class="mt-1 text-xs text-red-600">Impacted people count is required.</p>
              <p *ngIf="!peopleModel.invalid && create.impactedPeopleCount < 0 && peopleModel.touched" class="mt-1 text-xs text-red-600">Impacted people count cannot be negative.</p>
            </div>
          </div>

          <div>
            <label for="impact" class="block text-sm font-semibold text-slate-800">
              Business Impact <span class="text-red-500">*</span>
            </label>
            <input
              id="impact"
              name="impact"
              #impactModel="ngModel"
              [(ngModel)]="create.impact"
              required
              class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
              [class.border-red-300]="impactModel.invalid && impactModel.touched"
              [class.border-slate-300]="!(impactModel.invalid && impactModel.touched)"
            />
            <p *ngIf="impactModel.invalid && impactModel.touched" class="mt-1 text-xs text-red-600">Business impact is required.</p>
          </div>

          <div>
            <label for="createNotes" class="block text-sm font-medium text-slate-700">
              Notes <span class="text-slate-400">(Optional)</span>
            </label>
            <textarea
              id="createNotes"
              name="createNotes"
              [(ngModel)]="create.notes"
              rows="3"
              class="mt-1.5 w-full rounded-xl border border-slate-300 px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-100"
            ></textarea>
          </div>

          <button
            type="submit"
            [disabled]="createForm.invalid || create.impactedPeopleCount < 0 || loading"
            class="rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 disabled:opacity-60"
          >
            Submit Request
          </button>
        </form>

        <form *ngIf="mode === 'inspection'" class="mt-5 space-y-5" (ngSubmit)="submitInspection(inspectionForm)" #inspectionForm="ngForm" novalidate>
          <div>
            <label for="findings" class="block text-sm font-semibold text-slate-800">
              Findings <span class="text-red-500">*</span>
            </label>
            <textarea
              id="findings"
              name="findings"
              #findingsModel="ngModel"
              [(ngModel)]="inspection.findings"
              required
              rows="4"
              class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
              [class.border-red-300]="findingsModel.invalid && findingsModel.touched"
              [class.border-slate-300]="!(findingsModel.invalid && findingsModel.touched)"
            ></textarea>
            <p *ngIf="findingsModel.invalid && findingsModel.touched" class="mt-1 text-xs text-red-600">Findings are required.</p>
          </div>

          <div class="grid gap-5 sm:grid-cols-2">
            <div>
              <label for="estimatedCost" class="block text-sm font-semibold text-slate-800">
                Estimated Cost <span class="text-red-500">*</span>
              </label>
              <input
                id="estimatedCost"
                name="estimatedCost"
                type="number"
                min="0.01"
                step="0.01"
                #estimatedCostModel="ngModel"
                [(ngModel)]="inspection.estimatedCost"
                required
                class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
                [class.border-red-300]="(estimatedCostModel.invalid || inspection.estimatedCost <= 0) && estimatedCostModel.touched"
                [class.border-slate-300]="!((estimatedCostModel.invalid || inspection.estimatedCost <= 0) && estimatedCostModel.touched)"
              />
              <p *ngIf="estimatedCostModel.invalid && estimatedCostModel.touched" class="mt-1 text-xs text-red-600">Estimated cost is required.</p>
              <p *ngIf="!estimatedCostModel.invalid && inspection.estimatedCost <= 0 && estimatedCostModel.touched" class="mt-1 text-xs text-red-600">Estimated cost must be greater than 0.</p>
            </div>

            <div>
              <label for="estimatedTime" class="block text-sm font-semibold text-slate-800">
                Estimated Time <span class="text-red-500">*</span>
              </label>
              <input
                id="estimatedTime"
                name="estimatedTime"
                #estimatedTimeModel="ngModel"
                [(ngModel)]="inspection.estimatedTime"
                required
                placeholder="2 business days"
                class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
                [class.border-red-300]="estimatedTimeModel.invalid && estimatedTimeModel.touched"
                [class.border-slate-300]="!(estimatedTimeModel.invalid && estimatedTimeModel.touched)"
              />
              <p *ngIf="estimatedTimeModel.invalid && estimatedTimeModel.touched" class="mt-1 text-xs text-red-600">Estimated time is required.</p>
            </div>
          </div>

          <div>
            <label for="inspectionDate" class="block text-sm font-semibold text-slate-800">
              Inspection Date <span class="text-red-500">*</span>
            </label>
            <input
              id="inspectionDate"
              name="inspectionDate"
              type="date"
              [max]="today"
              #inspectionDateModel="ngModel"
              [(ngModel)]="inspection.inspectionDate"
              required
              class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
              [class.border-red-300]="(inspectionDateModel.invalid || isFutureInspectionDate()) && inspectionDateModel.touched"
              [class.border-slate-300]="!((inspectionDateModel.invalid || isFutureInspectionDate()) && inspectionDateModel.touched)"
            />
            <p *ngIf="inspectionDateModel.invalid && inspectionDateModel.touched" class="mt-1 text-xs text-red-600">Inspection date is required.</p>
            <p *ngIf="!inspectionDateModel.invalid && isFutureInspectionDate() && inspectionDateModel.touched" class="mt-1 text-xs text-red-600">Inspection date cannot be in the future.</p>
          </div>

          <div>
            <label for="inspectionNotes" class="block text-sm font-medium text-slate-700">
              Additional Notes <span class="text-slate-400">(Optional)</span>
            </label>
            <textarea
              id="inspectionNotes"
              name="inspectionNotes"
              [(ngModel)]="inspection.notes"
              rows="3"
              class="mt-1.5 w-full rounded-xl border border-slate-300 px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-100"
            ></textarea>
          </div>

          <button
            type="submit"
            [disabled]="inspectionForm.invalid || inspection.estimatedCost <= 0 || isFutureInspectionDate() || loading"
            class="rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 disabled:opacity-60"
          >
            Submit Inspection
          </button>
        </form>

        <form *ngIf="mode === 'decision'" class="mt-5 space-y-5" (ngSubmit)="submitDecision(decisionForm)" #decisionForm="ngForm" novalidate>
          <section *ngIf="details" class="rounded-2xl bg-slate-50 p-4">
            <h2 class="text-sm font-semibold text-slate-800">Request and Inspection</h2>
            <dl class="mt-3 grid gap-3 sm:grid-cols-2">
              <div><dt class="text-xs font-semibold uppercase text-slate-500">Vehicle</dt><dd class="mt-1 text-sm">{{ details.vehicleId }}</dd></div>
              <div><dt class="text-xs font-semibold uppercase text-slate-500">Cost</dt><dd class="mt-1 text-sm">{{ details.estimatedCost || '-' }}</dd></div>
              <div class="sm:col-span-2"><dt class="text-xs font-semibold uppercase text-slate-500">Description</dt><dd class="mt-1 text-sm leading-6">{{ details.description }}</dd></div>
              <div class="sm:col-span-2"><dt class="text-xs font-semibold uppercase text-slate-500">Inspection</dt><dd class="mt-1 text-sm leading-6">{{ details.inspectionReport || '-' }}</dd></div>
            </dl>
          </section>

          <div>
            <label for="remarks" class="block text-sm font-semibold text-slate-800">
              Remarks <span class="text-red-500">*</span>
            </label>
            <textarea
              id="remarks"
              name="remarks"
              #remarksModel="ngModel"
              [(ngModel)]="remarks"
              required
              rows="4"
              class="mt-1.5 w-full rounded-xl border px-3 py-2.5 text-sm focus:outline-none focus:ring-4 focus:ring-blue-100"
              [class.border-red-300]="remarksModel.invalid && remarksModel.touched"
              [class.border-slate-300]="!(remarksModel.invalid && remarksModel.touched)"
            ></textarea>
            <p *ngIf="remarksModel.invalid && remarksModel.touched" class="mt-1 text-xs text-red-600">Remarks are required.</p>
          </div>

          <div>
            <label for="decisionNotes" class="block text-sm font-medium text-slate-700">
              Internal Notes <span class="text-slate-400">(Optional)</span>
            </label>
            <textarea
              id="decisionNotes"
              name="decisionNotes"
              [(ngModel)]="decisionNotes"
              rows="3"
              class="mt-1.5 w-full rounded-xl border border-slate-300 px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-100"
            ></textarea>
          </div>

          <div class="flex flex-wrap gap-2">
            <button type="button" (click)="decisionType = 'APPROVE'; submitDecision(decisionForm)" [disabled]="decisionForm.invalid || loading" class="rounded-xl bg-green-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-green-200 transition hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-600 focus:ring-offset-2 disabled:opacity-60">Approve</button>
            <button type="button" (click)="decisionType = 'REJECT'; submitDecision(decisionForm)" [disabled]="decisionForm.invalid || loading" class="rounded-xl bg-red-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-red-200 transition hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-600 focus:ring-offset-2 disabled:opacity-60">Reject</button>
            <button type="button" (click)="decisionType = 'REQUEST_MORE_INFO'; submitDecision(decisionForm)" [disabled]="decisionForm.invalid || loading" class="rounded-xl bg-yellow-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-yellow-200 transition hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-yellow-600 focus:ring-offset-2 disabled:opacity-60">Request More Info</button>
          </div>
        </form>
      </section>
    </main>
  `
})
export class RequestFormComponent implements OnInit {
  mode: FormMode = 'create';
  id = '';
  loading = false;
  error = '';
  details: MaintenanceDetails | null = null;
  remarks = '';
  decisionNotes = '';
  decisionType: DecisionType = 'APPROVE';
  today = new Date().toISOString().slice(0, 10);

  create = {
    vehicleId: '',
    description: '',
    severity: 'LOW',
    impact: '',
    impactedPeopleCount: 0,
    notes: ''
  };

  inspection = {
    findings: '',
    estimatedCost: 0,
    estimatedTime: '',
    inspectionDate: new Date().toISOString().slice(0, 10),
    notes: ''
  };

  constructor(
    readonly auth: AuthService,
    private readonly api: FleetApiService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {
  }

  ngOnInit(): void {
    const path = this.router.url;
    this.id = this.route.snapshot.paramMap.get('id') ?? '';
    this.mode = path.startsWith('/inspection-form') ? 'inspection' : path.startsWith('/decision-form') ? 'decision' : 'create';
    if (this.id) {
      this.api.details(this.id).subscribe({
        next: (details) => this.details = details,
        error: () => this.error = 'Unable to load request details.'
      });
    }
  }

  title(): string {
    if (this.mode === 'inspection') {
      return 'Submit Inspection';
    }
    if (this.mode === 'decision') {
      return 'Approve or Reject';
    }
    return 'Create Maintenance Request';
  }

  subtitle(): string {
    if (this.mode === 'inspection') {
      return 'Capture findings, pricing, and the inspection date before sending the estimate.';
    }
    if (this.mode === 'decision') {
      return 'Review the request and submit the final service authorization outcome.';
    }
    return 'Log a new issue with enough detail for the provider to take over quickly.';
  }

  isFutureInspectionDate(): boolean {
    return Boolean(this.inspection.inspectionDate) && this.inspection.inspectionDate > this.today;
  }

  submitCreate(form: NgForm): void {
    if (form.invalid === true || this.create.impactedPeopleCount < 0) {
      form.control.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.api.createRequest(this.create).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error = 'Unable to create request.';
        this.loading = false;
      }
    });
  }

  submitInspection(form: NgForm): void {
    if (form.invalid === true || this.inspection.estimatedCost <= 0 || this.isFutureInspectionDate()) {
      form.control.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.api.submitInspection(this.id, this.inspection).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error = 'Unable to submit inspection.';
        this.loading = false;
      }
    });
  }

  submitDecision(form: NgForm): void {
    if (form.invalid === true) {
      form.control.markAllAsTouched();
      return;
    }
    this.loading = true;
    const notesSuffix = this.decisionNotes.trim() ? `\n\nInternal notes: ${this.decisionNotes.trim()}` : '';
    this.api.submitDecision(this.id, this.decisionType, `${this.remarks}${notesSuffix}`).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error = 'Unable to submit decision.';
        this.loading = false;
      }
    });
  }
}
