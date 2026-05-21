import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export type UserRole = 'COORDINATOR' | 'PROVIDER';

export interface LoginResponse {
  username: string;
  token: string;
  role: UserRole;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiBase = '/api';

  constructor(private readonly http: HttpClient) {
  }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiBase}/auth/login`, { username, password }).pipe(
      tap((response) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        localStorage.setItem('username', response.username);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
  }

  isLoggedIn(): boolean {
    return Boolean(localStorage.getItem('token'));
  }

  token(): string | null {
    return localStorage.getItem('token');
  }

  role(): UserRole | null {
    return localStorage.getItem('role') as UserRole | null;
  }

  username(): string {
    return localStorage.getItem('username') ?? '';
  }
}
