import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginResponse {
  accessToken: string;
}

export interface UtilisateurConnecte {
  id: string;
  roles: string[];
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

  moi(): Observable<UtilisateurConnecte> {
    return this.http.get<UtilisateurConnecte>('/api/auth/me');
  }

}
