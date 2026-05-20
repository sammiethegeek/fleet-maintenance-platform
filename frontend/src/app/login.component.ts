import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <main class="min-h-screen bg-[radial-gradient(circle_at_top_left,_rgba(59,130,246,0.14),_transparent_32%),linear-gradient(180deg,_#f8fafc_0%,_#eef4ff_100%)] px-4 py-10">
      <section class="mx-auto flex min-h-[80vh] max-w-5xl items-center justify-center">
        <div class="grid w-full max-w-4xl gap-8 lg:grid-cols-[1.15fr_0.85fr]">
          <aside class="hidden rounded-[28px] bg-slate-900 px-8 py-10 text-white shadow-2xl lg:block">
            <p class="text-sm font-semibold uppercase tracking-[0.24em] text-blue-200">Smart Fleet</p>
            <h1 class="mt-5 text-4xl font-semibold leading-tight">Maintenance decisions without the back-and-forth.</h1>
            <p class="mt-4 max-w-md text-sm leading-6 text-slate-300">
              Track requests, route work to providers, and move repairs to approval in one simple flow.
            </p>
            <div class="mt-10 space-y-3 text-sm text-slate-200">
              <div class="rounded-2xl border border-white/10 bg-white/5 px-4 py-3">Coordinator: create requests, assign provider, approve or reject.</div>
              <div class="rounded-2xl border border-white/10 bg-white/5 px-4 py-3">Provider: review assignments and submit inspection estimates.</div>
            </div>
          </aside>

          <section class="w-full rounded-[28px] border border-white/70 bg-white/90 p-6 shadow-xl shadow-blue-100/60 backdrop-blur" aria-labelledby="login-title">
            <h1 id="login-title" class="text-3xl font-semibold text-slate-900">Sign in</h1>
            <p class="mt-2 text-sm leading-6 text-slate-600">Use your coordinator or provider account to enter the workflow demo.</p>

            <form class="mt-6 space-y-4" (ngSubmit)="submit()" #loginForm="ngForm">
              <div>
                <label for="username" class="block text-sm font-semibold text-slate-800">
                  Username <span class="text-red-500">*</span>
                </label>
                <input
                  id="username"
                  name="username"
                  #usernameModel="ngModel"
                  [(ngModel)]="username"
                  required
                  autocomplete="username"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
                  [class.border-red-300]="usernameModel.invalid && usernameModel.touched"
                  [class.border-slate-300]="!(usernameModel.invalid && usernameModel.touched)"
                />
                <p *ngIf="usernameModel.invalid && usernameModel.touched" class="mt-1 text-xs text-red-600">Username is required.</p>
              </div>

              <div>
                <label for="password" class="block text-sm font-semibold text-slate-800">
                  Password <span class="text-red-500">*</span>
                </label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  #passwordModel="ngModel"
                  [(ngModel)]="password"
                  required
                  autocomplete="current-password"
                  class="mt-1.5 w-full rounded-xl border border-slate-300 px-3 py-2.5 text-sm outline-none transition focus:border-blue-600 focus:ring-4 focus:ring-blue-100"
                  [class.border-red-300]="passwordModel.invalid && passwordModel.touched"
                  [class.border-slate-300]="!(passwordModel.invalid && passwordModel.touched)"
                />
                <p *ngIf="passwordModel.invalid && passwordModel.touched" class="mt-1 text-xs text-red-600">Password is required.</p>
              </div>

              <p *ngIf="error" class="rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">{{ error }}</p>

              <button
                type="submit"
                [disabled]="loginForm.invalid || loading"
                class="w-full rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold text-white shadow-lg shadow-blue-200 transition hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-600 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {{ loading ? 'Signing in...' : 'Login' }}
              </button>
            </form>

          </section>
        </div>
      </section>
    </main>
  `
})
export class LoginComponent {
  username = '';
  password = '';
  loading = false;
  error = '';

  constructor(private readonly auth: AuthService, private readonly router: Router) {
  }

  submit(): void {
    this.loading = true;
    this.error = '';
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error = 'Login failed. Check the username and password.';
        this.loading = false;
      }
    });
  }
}
