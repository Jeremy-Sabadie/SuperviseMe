import { Component, inject, OnInit, signal } from '@angular/core';
import Swal from 'sweetalert2';
import { DossierApi, DossierApiService } from '../../core/services/dossier-api';
import { ClientApiService } from '../../core/services/client-api';
import { Router, RouterLink } from '@angular/router';
import { SessionService } from '../../core/auth/session';
import { AuthApiService } from '../../core/services/auth-api';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';

type Statut = 'A_TRAITER' | 'EN_COURS' | 'TERMINE';

interface Dossier {
  id: string;
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
export class Kanban implements OnInit {
  private readonly session = inject(SessionService);
  private readonly dossierApi = inject(DossierApiService);
  private readonly clientApi = inject(ClientApiService);
  readonly chargement = signal(false);
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

  readonly dossiers = signal<Dossier[]>([]);

  ngOnInit(): void {
    this.chargerDossiers();
  }

  chargerDossiers(): void {
    this.chargement.set(true);

    this.clientApi.lister().subscribe({
      next: clients => {
        const noms = new Map(
          clients.map(client => [
            client.id,
            client.entreprise || client.nom
          ])
        );

        this.dossierApi.lister().subscribe({
          next: dossiers => {
            this.dossiers.set(
              dossiers
                .filter(d => d.statut !== 'ARCHIVE')
                .map(d => ({
                  id: d.id,
                  titre: d.titre,
                  client: (d.clientId ? noms.get(d.clientId) : null) ?? 'Client non renseigné',
                  priorite: d.priorite === 'HAUTE'
                    ? 'Haute'
                    : d.priorite === 'BASSE' ? 'Basse' : 'Normale',
                  responsable: d.responsableId ? 'Responsable' : 'Non assigné',
                  statut: d.statut as Statut
                }))
            );
            this.chargement.set(false);
          },
          error: () => this.erreurChargement()
        });
      },
      error: () => this.erreurChargement()
    });
  }

  private erreurChargement(): void {
    this.chargement.set(false);
    void Swal.fire({
      icon: 'error',
      title: 'Chargement impossible',
      text: 'Impossible de récupérer les dossiers ou les clients.',
      background: '#151d30',
      color: '#ffffff'
    });
  }

  dossiersParStatut(statut: Statut): Dossier[] {
    return this.dossiers().filter(dossier => dossier.statut === statut);
  }

  deplacer(event: CdkDragDrop<Dossier[]>, statut: Statut): void {
    if (event.previousContainer === event.container) return;

    const dossier = event.previousContainer.data[event.previousIndex];
    if (!dossier) return;

    const ancienStatut = dossier.statut;

    this.dossiers.update(dossiers =>
      dossiers.map(item =>
        item.id === dossier.id ? { ...item, statut } : item
      )
    );

    this.dossierApi.changerStatut(dossier.id, statut).subscribe({
      error: () => {
        this.dossiers.update(dossiers =>
          dossiers.map(item =>
            item.id === dossier.id
              ? { ...item, statut: ancienStatut }
              : item
          )
        );

        void Swal.fire({
          icon: 'error',
          title: 'Déplacement impossible',
          text: 'Le statut précédent a été restauré.',
          background: '#151d30',
          color: '#ffffff'
        });
      }
    });
  }
}
