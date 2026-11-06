import { Injectable, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { DossierApi, DossierApiService } from '../../../core/services/dossier-api';

export class DossierDejaAssocieError extends Error {
  constructor() {
    super('Ce dossier appartient déjà à un client.');
  }
}

@Injectable({ providedIn: 'root' })
export class AssociationDossierService {
  private readonly dossiersApi = inject(DossierApiService);

  associer(dossierId: string, clientId: string): Observable<DossierApi> {
    return this.dossiersApi.associerClient(dossierId, clientId).pipe(
      catchError(erreur => {
        if (erreur instanceof HttpErrorResponse && erreur.status === 409) {
          return throwError(() => new DossierDejaAssocieError());
        }
        return throwError(() => erreur);
      })
    );
  }
}
