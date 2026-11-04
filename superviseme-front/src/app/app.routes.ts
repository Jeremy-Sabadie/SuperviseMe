import { adminGuard } from './core/auth/admin.guard';
import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'collaborateurs',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/collaborateurs/collaborateurs')
        .then(m => m.Collaborateurs)
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login').then(m => m.Login)
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/kanban/kanban').then(m => m.Kanban)
  }
];
