import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { DossierApiService } from './dossier-api';

describe('DossierApiService', () => {
  let service: DossierApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(DossierApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('récupère les dossiers depuis l’API', () => {
    service.lister().subscribe(dossiers => {
      expect(dossiers.length).toBe(1);
      expect(dossiers[0].statut).toBe('A_TRAITER');
    });

    const req = http.expectOne('/api/dossiers');
    expect(req.request.method).toBe('GET');

    req.flush([
      {
        id: '550e8400-e29b-41d4-a716-446655440000',
        titre: 'Dossier test',
        statut: 'A_TRAITER',
        priorite: 'NORMALE'
      }
    ]);
  });

  it('modifie uniquement le statut d’un dossier', () => {
    const id = '550e8400-e29b-41d4-a716-446655440000';

    service.changerStatut(id, 'EN_COURS').subscribe();

    const req = http.expectOne(`/api/dossiers/${id}/statut`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ statut: 'EN_COURS' });

    req.flush({ id, statut: 'EN_COURS' });
  });

  it('associe un client à un dossier via PATCH', () => {
    service.associerClient('d1', 'c1').subscribe();

    const req = http.expectOne('/api/dossiers/d1/client');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ clientId: 'c1' });

    req.flush({ id: 'd1', clientId: 'c1' });
  });


  it('transmet plusieurs participants via PUT', () => {
    const id = 'dossier-1';
    const participantIds = ['user-1', 'user-2'];

    service.affecterParticipants(id, participantIds).subscribe(dossier => {
      expect(dossier.participantIds).toEqual(participantIds);
    });

    const requete = http.expectOne(`/api/dossiers/${id}/participants`);

    expect(requete.request.method).toBe('PUT');
    expect(requete.request.body).toEqual({ participantIds });

    requete.flush({ id, participantIds });
  });

  it('propage une erreur HTTP lors de l’attribution', () => {
    let codeErreur: number | undefined;

    service.affecterParticipants('dossier-1', ['user-1']).subscribe({
      next: () => { throw new Error('La requête devait échouer'); },
      error: erreur => {
        codeErreur = erreur.status;
      }
    });

    const requete = http.expectOne('/api/dossiers/dossier-1/participants');

    // Le backend refuse l'attribution à un utilisateur non autorisé.
    requete.flush(
      { message: 'Accès refusé' },
      { status: 403, statusText: 'Forbidden' }
    );

    expect(codeErreur).toBe(403);
  });


});
