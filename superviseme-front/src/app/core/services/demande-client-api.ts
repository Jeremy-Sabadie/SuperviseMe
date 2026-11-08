import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StatutDemande =
  | 'A_TRAITER'
  | 'EN_COURS'
  | 'TRAITE'
  | 'PROPOSITION_COMMERCIALE_A_ETABLIR'
  | 'PROPOSITION_COMMERCIALE_ENVOYEE'
  | 'PROPOSITION_COMMERCIALE_ACCEPTEE'
  | 'PROPOSITION_COMMERCIALE_REFUSEE';

export interface DemandeClient {
  id: string;
  clientId: string;
  nom: string;
  email: string;
  motif: string;
  message: string;
  statut: StatutDemande;
  dateCreation: string;
}

@Injectable({ providedIn: 'root' })
export class DemandeClientApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/demandes-clients';

  lister(): Observable<DemandeClient[]> {
    return this.http.get<DemandeClient[]>(this.url);
  }

  compteur(): Observable<number> {
    return this.http.get<number>(`${this.url}/compteur`);
  }

  changerStatut(
    id: string,
    statut: StatutDemande
  ): Observable<DemandeClient> {
    return this.http.patch<DemandeClient>(
      `${this.url}/${id}/statut`,
      { statut }
    );
  }
}
