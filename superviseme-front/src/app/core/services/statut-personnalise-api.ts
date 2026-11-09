import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CategorieStatut,
  StatutPersonnalise
} from './statut-personnalise.model';

/**
 * Centralise la consultation des statuts configurés
 * pour les clients et les dossiers.
 */
@Injectable({ providedIn: 'root' })
export class StatutPersonnaliseApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/admin/statuts';

  /**
   * Récupère les statuts d'une catégorie métier.
   */
  lister(categorie: CategorieStatut): Observable<StatutPersonnalise[]> {
    const params = new HttpParams().set('categorie', categorie);

    return this.http.get<StatutPersonnalise[]>(this.url, { params });
  }
}
