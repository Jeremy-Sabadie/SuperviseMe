import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/kanban/kanban').then(m => m.Kanban)
  }
];
