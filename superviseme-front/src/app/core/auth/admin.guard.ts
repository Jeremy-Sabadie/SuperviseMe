import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { SessionService } from './session';
import { AuthApiService } from '../services/auth-api';

export const adminGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);
  const auth = inject(AuthApiService);

  if (!session.token()) {
    return router.parseUrl('/login');
  }

  return auth.moi().pipe(
    map(utilisateur =>
      utilisateur.roles.includes('ADMIN')
        ? true
        : router.parseUrl('/')
    ),
    catchError(() => of(router.parseUrl('/')))
  );
};
