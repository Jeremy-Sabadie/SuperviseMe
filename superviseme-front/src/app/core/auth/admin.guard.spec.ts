import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SessionService } from './session';
import { AuthApiService } from '../services/auth-api';
import { adminGuard } from './admin.guard';

describe('adminGuard', () => {
  let session: SessionService;
  let router: Router;
  let roles: string[];
  let erreurApi: boolean;

  beforeEach(() => {
    roles = [];
    erreurApi = false;

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthApiService,
          useValue: {
            moi: () => erreurApi
              ? throwError(() => new Error('API indisponible'))
              : of({ id: 'uuid-test', roles })
          }
        }
      ]
    });

    session = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
  });

  const verifierAcces = () =>
    TestBed.runInInjectionContext(
      () => adminGuard({} as never, {} as never)
    );

  it('redirige vers login sans JWT', () => {
    expect(verifierAcces()).toEqual(router.parseUrl('/login'));
  });

  it('autorise un ADMIN connecté', async () => {
    session.definirToken('jwt-test');
    roles = ['ADMIN'];

    await expect(Promise.resolve(verifierAcces())).resolves.toBe(true);
  });

  it('refuse un USER connecté', async () => {
    session.definirToken('jwt-test');
    roles = ['USER'];

    await expect(Promise.resolve(verifierAcces()))
      .resolves.toEqual(router.parseUrl('/'));
  });

  it('refuse l’accès lorsque l’API échoue', async () => {
    session.definirToken('jwt-test');
    erreurApi = true;

    await expect(Promise.resolve(verifierAcces()))
      .resolves.toEqual(router.parseUrl('/'));
  });
});
