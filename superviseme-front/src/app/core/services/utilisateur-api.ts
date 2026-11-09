import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CollaborateurResponse {
  id: string;
  email: string;
  prenom: string | null;
  nom: string | null;
  role: string;
  actif: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UtilisateurApiService {
  private readonly http = inject(HttpClient);

  lister(): Observable<CollaborateurResponse[]> {
    return this.http.get<CollaborateurResponse[]>('/api/utilisateurs');
  }

  creer(email: string, motDePasse: string, prenom: string, nom: string): Observable<CollaborateurResponse> {
    return this.http.post<CollaborateurResponse>('/api/utilisateurs', {
      email,
      motDePasse,
      prenom,
      nom
    });
  }

  /** Désactive un compte sans supprimer son historique. */
  modifier(id: string, email: string, prenom: string, nom: string) {
    return this.http.put<CollaborateurResponse>(
      `/api/utilisateurs/${id}`,
      { email, prenom, nom }
    );
  }

  desactiver(id: string): Observable<CollaborateurResponse> {
    return this.http.patch<CollaborateurResponse>(
      `/api/utilisateurs/${encodeURIComponent(id)}/desactiver`, {}
    );
  }

  /** Réactive un compte existant. */
  reactiver(id: string): Observable<CollaborateurResponse> {
    return this.http.patch<CollaborateurResponse>(
      `/api/utilisateurs/${encodeURIComponent(id)}/reactiver`, {}
    );
  }

}
