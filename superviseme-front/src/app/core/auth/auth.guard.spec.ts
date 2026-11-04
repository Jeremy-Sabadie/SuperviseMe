import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { SessionService } from './session';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let session: SessionService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([])]
    });

    session = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
  });

  it('redirige vers login sans JWT', () => {
    const resultat = TestBed.runInInjectionContext(
      () => authGuard({} as never, {} as never)
    );

    expect(resultat).toEqual(router.parseUrl('/login'));
  });

  it('autorise le Kanban avec un JWT', () => {
    session.definirToken('jwt-test');

    const resultat = TestBed.runInInjectionContext(
      () => authGuard({} as never, {} as never)
    );

    expect(resultat).toBe(true);
  });
});
