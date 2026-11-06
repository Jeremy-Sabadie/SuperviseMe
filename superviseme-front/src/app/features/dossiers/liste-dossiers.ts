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
  template: `
    <section class="liste-dossiers">
      <header>
        <div>
          <h1>Mes dossiers</h1>
          <p>Consultez et retrouvez vos dossiers.</p>
        </div>
        @if (estAdmin()) {
          <a routerLink="/dossiers/nouveau" class="bouton">+ Nouveau dossier</a>
        }
      </header>

      <input
        type="search"
        placeholder="Rechercher un dossier ou un client..."
        aria-label="Rechercher un dossier"
        [ngModel]="recherche()"
        (ngModelChange)="recherche.set($event)"
      />

      @if (chargement()) {
        <p>Chargement des dossiers...</p>
      } @else if (erreur()) {
        <p role="alert">{{ erreur() }}</p>
      } @else if (dossiersFiltres().length === 0) {
        <p>Aucun dossier trouvé.</p>
      } @else {
        <div class="liste">
          @for (dossier of dossiersFiltres(); track dossier.id) {
            <a [routerLink]="['/dossiers', dossier.id]" class="carte">
              <strong>{{ dossier.titre }}</strong>
              <span>{{ nomClient(dossier.clientId) }}</span>
              <small>{{ dossier.statut.replaceAll('_', ' ') }}</small>
            </a>
          }
        </div>
      }
    </section>
  `,
  styles: [`
    .liste-dossiers { max-width: 1100px; margin: 0 auto; padding: 24px; }
    header { display: flex; justify-content: space-between; align-items: center;
      gap: 16px; flex-wrap: wrap; margin-bottom: 24px; }
    h1 { margin: 0 0 8px; }
    header p { margin: 0; opacity: .7; }
    .bouton { background: #6558f5; color: white; padding: 12px 18px;
      border-radius: 10px; text-decoration: none; }
    input { width: 100%; padding: 14px; border-radius: 10px;
      border: 1px solid #475569; background: #151d30;
      color: white; margin-bottom: 24px; box-sizing: border-box; }
    .liste { display: grid; gap: 12px; }
    .carte { display: flex; justify-content: space-between; align-items: center;
      gap: 12px; flex-wrap: wrap; padding: 18px; border-radius: 12px;
      background: #1e293b; color: white; text-decoration: none; }
    .carte:hover { background: #334155; }
    .carte span { opacity: .8; }
    .carte small { color: #a5b4fc; }
  `]
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

  nomClient(id: string | null): string {
    return (id ? this.clients().get(id) : null) ?? 'Client non renseigné';
  }
}
