import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { FicheDossier } from './fiche-dossier';
import { DossierApiService } from '../../../core/services/dossier-api';

describe('FicheDossier', () => {
  it('charge le dossier demandé par son identifiant', () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'dossiers/:id', component: FicheDossier }])
      ]
    });

    const api = TestBed.inject(DossierApiService);
    const http = TestBed.inject(HttpTestingController);

    api.consulter('dossier-123').subscribe(dossier => {
      expect(dossier.titre).toBe('Chantier exemple');
    });

    http.expectOne('/api/dossiers/dossier-123').flush({
      id: 'dossier-123',
      titre: 'Chantier exemple'
    });

    http.verify();
  });

  it('envoie les modifications du dossier avec PUT', () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    const api = TestBed.inject(DossierApiService);
    const http = TestBed.inject(HttpTestingController);

    api.modifier('dossier-123', {
      titre: 'Nouveau titre',
      description: 'Description',
      statut: 'EN_COURS',
      priorite: 'HAUTE',
      echeance: null,
      clientId: null
    }).subscribe();

    const requete = http.expectOne('/api/dossiers/dossier-123');
    expect(requete.request.method).toBe('PUT');
    expect(requete.request.body.titre).toBe('Nouveau titre');
    requete.flush({});
    http.verify();
  });
});
