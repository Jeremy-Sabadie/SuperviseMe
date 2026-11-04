import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginResponse {
  accessToken: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private readonly http = inject(HttpClient);

  connecter(email: string, motDePasse: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', {
      email,
      motDePasse
    });
  }
}
