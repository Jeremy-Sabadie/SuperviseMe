import { TestBed } from '@angular/core/testing';
import { SessionService } from './session';

describe('SessionService', () => {
  let session: SessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    session = TestBed.inject(SessionService);
  });

  it('conserve le JWT en mémoire', () => {
    session.definirToken('jwt-test');
    expect(session.token()).toBe('jwt-test');
  });

  it('supprime le JWT à la déconnexion', () => {
    session.definirToken('jwt-test');
    session.deconnecter();
    expect(session.token()).toBeNull();
  });
});
