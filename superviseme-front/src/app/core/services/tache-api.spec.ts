import { TestBed } from '@angular/core/testing';
import {
  provideHttpClient,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { TacheApiService } from './tache-api';

describe('TacheApiService', () => {
  let service: TacheApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(TacheApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('récupère les tâches de l’entreprise', () => {
    service.lister().subscribe(taches => {
      expect(taches).toEqual([]);
    });

    const requete = http.expectOne('/api/taches');

    expect(requete.request.method).toBe('GET');
    requete.flush([]);
  });

  it('crée une tâche avec son responsable', () => {
    const demande = {
      titre: 'Préparer commande',
      dossierId: null,
      responsableId: 'responsable-123',
    };

    service.creer(demande).subscribe();

    const requete = http.expectOne('/api/taches');

    expect(requete.request.method).toBe('POST');
    expect(requete.request.body).toEqual(demande);

    requete.flush({
      id: 'tache-123',
      ...demande,
      statut: 'NON_COMMENCEE',
      priorite: 'NORMALE',
    });
  });

  it('modifie uniquement le statut demandé', () => {
    service.changerStatut('tache-123', 'EN_COURS').subscribe();

    const requete = http.expectOne('/api/taches/tache-123/statut');

    expect(requete.request.method).toBe('PATCH');
    expect(requete.request.body).toEqual({
      statut: 'EN_COURS',
    });

    requete.flush({
      id: 'tache-123',
      statut: 'EN_COURS',
    });
  });
});
