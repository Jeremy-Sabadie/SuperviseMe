import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type StatutTache =
  | 'NON_COMMENCEE'
  | 'EN_COURS'
  | 'TERMINEE';

export type PrioriteTache =
  | 'BASSE'
  | 'NORMALE'
  | 'HAUTE';

/** Représentation d'une tâche renvoyée par l'API REST. */
export interface TacheApi {
  id: string;
  titre: string;
  description: string | null;
  dossierId: string | null;
  responsableId: string | null;
  statut: StatutTache;
  priorite: PrioriteTache;
  echeance: string | null;
  dateCreation: string;
  dateModification: string;
}

/** Données nécessaires à la création d'une tâche. */
export interface CreerTacheRequest {
  titre: string;
  dossierId: string | null;
  responsableId: string;
}

@Injectable({ providedIn: 'root' })
export class TacheApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/taches';

  lister(): Observable<TacheApi[]> {
    return this.http.get<TacheApi[]>(this.url);
  }

  creer(demande: CreerTacheRequest): Observable<TacheApi> {
    return this.http.post<TacheApi>(this.url, demande);
  }

  changerStatut(id: string, statut: StatutTache): Observable<TacheApi> {
    return this.http.patch<TacheApi>(
      `${this.url}/${encodeURIComponent(id)}/statut`,
      { statut }
    );
  }
}
