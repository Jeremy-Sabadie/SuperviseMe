import { TestBed } from '@angular/core/testing';
import {
  provideHttpClient,
  withInterceptorsFromDi
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { AuthApiService } from './auth-api';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envoie les identifiants à l’API', () => {
    service.connecter('test@example.com', 'secret').subscribe();

    const req = http.expectOne('/api/auth/login');

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'test@example.com',
      motDePasse: 'secret'
    });

    req.flush({ accessToken: 'jwt-test' });
  });

  it('récupère le jeton JWT renvoyé', () => {
    let token = '';

    service.connecter('test@example.com', 'secret')
      .subscribe(response => token = response.accessToken);

    http.expectOne('/api/auth/login')
      .flush({ accessToken: 'jwt-test' });

    expect(token).toBe('jwt-test');
  });

  it('récupère les rôles de l’utilisateur connecté', () => {
    let utilisateur: { id: string; roles: string[] } | undefined;

    service.moi().subscribe(response => utilisateur = response);

    const req = http.expectOne('/api/auth/me');
    expect(req.request.method).toBe('GET');

    req.flush({ id: 'uuid-test', roles: ['ADMIN'] });

    expect(utilisateur).toEqual({
      id: 'uuid-test',
      roles: ['ADMIN']
    });
  });
});
