import { adminGuard } from './core/auth/admin.guard';
import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'demandes-clients', canActivate: [authGuard], loadComponent: () => import('./features/demandes-clients/demandes-clients').then(m => m.DemandesClients) },
  {
    path: 'contact',
    loadComponent: () =>
      import('./features/contact/contact').then(m => m.Contact)
  },

  {
    path: 'dossiers/nouveau',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/dossiers/dossiers/dossiers')
      .then(m => m.Dossiers)
  },
  {
    path: 'dossiers/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dossiers/fiche-dossier/fiche-dossier')
        .then(m => m.FicheDossier)
  },
  {
    path: 'dossiers',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dossiers/liste-dossiers')
      .then(m => m.ListeDossiers)
  },
  {
    path: 'clients',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/clients/clients/clients')
        .then(m => m.Clients)
  },

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
