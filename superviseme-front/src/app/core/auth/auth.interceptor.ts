import { inject } from '@angular/core';
import {
  HttpInterceptorFn
} from '@angular/common/http';
import { SessionService } from './session';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const token = session.token();

  // Uniquement les routes API relatives de notre application.
  const apiInterne = req.url.startsWith('/api/');
  const connexion = req.url === '/api/auth/login';

  if (!apiInterne || connexion || !token) {
    return next(req);
  }

  return next(req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  }));
};
