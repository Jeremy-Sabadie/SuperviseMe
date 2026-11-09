import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { UtilisateurApiService } from './utilisateur-api';

describe('UtilisateurApiService', () => {
  let service: UtilisateurApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(UtilisateurApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('crée un collaborateur sans transmettre de rôle', () => {
    let resultat: { id: string; email: string; role: string } | undefined;

    service.creer('collaborateur@example.com', 'MotDePasse123!', 'Jean', 'Dupont').subscribe(
      response => resultat = response
    );

    const req = http.expectOne('/api/utilisateurs');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'collaborateur@example.com',
      motDePasse: 'MotDePasse123!',
        prenom: 'Jean',
        nom: 'Dupont'
    });

    req.flush({
      id: 'uuid-test',
      email: 'collaborateur@example.com',
      role: 'USER'
    });

    expect(resultat).toEqual({
      id: 'uuid-test',
      email: 'collaborateur@example.com',
      role: 'USER'
    });
  });
});
