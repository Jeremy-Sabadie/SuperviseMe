import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CollaborateurResponse {
  id: string;
  email: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class UtilisateurApiService {
  private readonly http = inject(HttpClient);

  creer(email: string, motDePasse: string): Observable<CollaborateurResponse> {
    return this.http.post<CollaborateurResponse>('/api/utilisateurs', {
      email,
      motDePasse
    });
  }
}
