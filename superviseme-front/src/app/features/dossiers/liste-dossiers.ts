import Swal from 'sweetalert2';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { DossierApi, DossierApiService } from '../../core/services/dossier-api';
import { ClientApiService } from '../../core/services/client-api';
import { AuthApiService } from '../../core/services/auth-api';

@Component({
  selector: 'app-liste-dossiers',
  imports: [RouterLink, FormsModule],
  templateUrl: './liste-dossiers.html',
  styleUrl: './liste-dossiers.scss'

})
export class ListeDossiers implements OnInit {
  private readonly api = inject(DossierApiService);
  private readonly clientsApi = inject(ClientApiService);
  private readonly auth = inject(AuthApiService);
  readonly estAdmin = signal(false);

  readonly dossiers = signal<DossierApi[]>([]);
  readonly clients = signal(new Map<string, string>());
  readonly recherche = signal('');
  readonly chargement = signal(true);
  readonly erreur = signal('');

  readonly dossiersFiltres = computed(() => {
    const terme = this.recherche().trim().toLocaleLowerCase();
    return this.dossiers().filter(dossier =>
      dossier.titre.toLocaleLowerCase().includes(terme) ||
      this.nomClient(dossier.clientId).toLocaleLowerCase().includes(terme)
    );
  });

  ngOnInit(): void {
    this.auth.moi().subscribe({
      next: utilisateur => this.estAdmin.set(utilisateur.roles.includes('ADMIN')),
      error: () => this.estAdmin.set(false)
    });

    forkJoin({
      dossiers: this.api.lister(),
      clients: this.clientsApi.lister()
    }).subscribe({
      next: ({ dossiers, clients }) => {
        this.clients.set(new Map(
          clients.map(c => [c.id, c.entreprise || c.nom])
        ));
        this.dossiers.set(dossiers);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les dossiers.');
        this.chargement.set(false);
      }
    });
  }

  async supprimer(dossier: DossierApi): Promise<void> {
    if (!this.estAdmin()) return;

    const confirmation = await Swal.fire({
      title: 'Supprimer ce dossier ?',
      text: `Le dossier « ${dossier.titre} » sera définitivement supprimé.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#c65d65',
      cancelButtonColor: '#344854',
      background: '#1b2a35',
      color: '#e5eef2',
      reverseButtons: true
    });

    if (!confirmation.isConfirmed) return;

    this.api.supprimer(dossier.id).subscribe({
      next: () => {
        this.dossiers.update(liste =>
          liste.filter(item => item.id !== dossier.id)
        );
        void Swal.fire({
          title: 'Dossier supprimé',
          icon: 'success',
          timer: 1500,
          showConfirmButton: false,
          background: '#1b2a35',
          color: '#e5eef2'
        });
      },
      error: () => {
        void Swal.fire({
          title: 'Suppression impossible',
          text: 'Le dossier n’a pas été supprimé.',
          icon: 'error',
          confirmButtonColor: '#4f9ca5',
          background: '#1b2a35',
          color: '#e5eef2'
        });
      }
    });
  }

  nomClient(id: string | null): string {
    return (id ? this.clients().get(id) : null) ?? 'Client non renseigné';
  }
}
