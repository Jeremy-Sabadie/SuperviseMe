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
});
