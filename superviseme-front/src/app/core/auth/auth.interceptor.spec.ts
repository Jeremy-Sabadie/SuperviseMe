import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SessionService } from './session';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let session: SessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    session = TestBed.inject(SessionService);
    session.definirToken('jwt-test');
  });

  afterEach(() => controller.verify());

  it('ajoute le JWT aux requêtes API internes', () => {
    http.get('/api/dossiers').subscribe();

    const req = controller.expectOne('/api/dossiers');
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-test');
    req.flush([]);
  });

  it('n’envoie pas le JWT à un domaine externe', () => {
    http.get('https://example.com/api/dossiers').subscribe();

    const req = controller.expectOne('https://example.com/api/dossiers');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it('n’ajoute pas de JWT à la connexion', () => {
    http.post('/api/auth/login', {}).subscribe();

    const req = controller.expectOne('/api/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ accessToken: 'nouveau-jwt' });
  });
});
