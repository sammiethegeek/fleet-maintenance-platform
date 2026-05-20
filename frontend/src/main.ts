import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Routes } from '@angular/router';
import { AppComponent } from './app/app.component';
import { LoginComponent } from './app/login.component';
import { DashboardComponent } from './app/dashboard.component';
import { RequestFormComponent } from './app/request-form.component';
import { authInterceptor } from './app/auth.interceptor';
import { authGuard } from './app/auth.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'request-form', component: RequestFormComponent, canActivate: [authGuard] },
  { path: 'inspection-form/:id', component: RequestFormComponent, canActivate: [authGuard] },
  { path: 'decision-form/:id', component: RequestFormComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' }
];

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
}).catch((error) => console.error(error));
