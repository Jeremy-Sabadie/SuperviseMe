import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StatutDossier =
  | 'A_TRAITER'
  | 'EN_COURS'
  | 'TERMINE'
  | 'ARCHIVE';

export type PrioriteDossier = 'BASSE' | 'NORMALE' | 'HAUTE';

export interface DossierApi {
  id: string;
  titre: string;
  description: string | null;
  statut: StatutDossier;
  priorite: PrioriteDossier;
  echeance: string | null;
  clientId: string;
  responsableId: string | null;
  participantIds: string[];
  dateCreation: string;
  dateModification: string;
}

export interface CreerDossierRequest {
  titre: string;
  description: string;
  clientId: string | null;
  responsableId: string;
}

@Injectable({ providedIn: 'root' })
export class DossierApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/dossiers';

  creer(dossier: CreerDossierRequest): Observable<DossierApi> {
    return this.http.post<DossierApi>(this.url, dossier);
  }

  lister(): Observable<DossierApi[]> {
    return this.http.get<DossierApi[]>(this.url);
  }

  changerStatut(id: string, statut: StatutDossier): Observable<DossierApi> {
    return this.http.patch<DossierApi>(
      `${this.url}/${id}/statut`,
      { statut }
    );
  }
}
