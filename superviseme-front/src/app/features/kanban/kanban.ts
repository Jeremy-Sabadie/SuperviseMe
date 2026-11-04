import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { SessionService } from '../../core/auth/session';
import { AuthApiService } from '../../core/services/auth-api';
import { CdkDragDrop, DragDropModule, transferArrayItem } from '@angular/cdk/drag-drop';

type Statut = 'A_TRAITER' | 'EN_COURS' | 'TERMINE';

interface Dossier {
  id: number;
  titre: string;
  client: string;
  priorite: string;
  responsable: string;
  statut: Statut;
}

@Component({
  selector: 'app-kanban',
  standalone: true,
  imports: [DragDropModule, RouterLink],
  templateUrl: './kanban.html',
  styleUrl: './kanban.scss'
})
export class Kanban {
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthApiService);
  readonly estAdmin = signal(false);

  constructor() {
    if (this.session.token()) {
      this.auth.moi().subscribe({
        next: utilisateur =>
          this.estAdmin.set(utilisateur.roles.includes('ADMIN')),
        error: () => this.estAdmin.set(false)
      });
    }
  }

  deconnecter(): void {
    this.session.deconnecter();
    this.router.navigateByUrl('/login');
  }

  readonly colonnes = [
    { id: 'A_TRAITER', titre: 'À traiter', couleur: '#a78bfa' },
    { id: 'EN_COURS', titre: 'En cours', couleur: '#38bdf8' },
    { id: 'TERMINE', titre: 'Terminé', couleur: '#34d399' }
  ] as const;

  readonly dossiers = signal<Dossier[]>([
    { id: 1, titre: 'Rénovation escalier', client: 'Maison Dupont', priorite: 'Haute', responsable: 'Romain', statut: 'A_TRAITER' },
    { id: 2, titre: 'Fabrication bibliothèque', client: 'Mme Martin', priorite: 'Normale', responsable: 'Romain', statut: 'A_TRAITER' },
    { id: 3, titre: 'Agencement boutique', client: 'Atelier Bernard', priorite: 'Haute', responsable: 'Équipe', statut: 'EN_COURS' },
    { id: 4, titre: 'Pose de menuiseries', client: 'M. Laurent', priorite: 'Normale', responsable: 'Romain', statut: 'TERMINE' }
  ]);

  dossiersParStatut(statut: Statut): Dossier[] {
    return this.dossiers().filter(dossier => dossier.statut === statut);
  }

  deplacer(event: CdkDragDrop<Dossier[]>, statut: Statut): void {
    if (event.previousContainer === event.container) {
      return;
    }

    const dossier = event.previousContainer.data[event.previousIndex];
    if (!dossier) return;

    this.dossiers.update(dossiers =>
      dossiers.map(item => item.id === dossier.id ? { ...item, statut } : item)
    );
  }
}
