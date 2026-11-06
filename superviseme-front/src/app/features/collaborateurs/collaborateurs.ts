import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  CollaborateurResponse,
  UtilisateurApiService
} from '../../core/services/utilisateur-api';

@Component({
  selector: 'app-collaborateurs',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './collaborateurs.html',
  styleUrl: './collaborateurs.scss'
})
export class Collaborateurs implements OnInit {
  private readonly api = inject(UtilisateurApiService);

  email = '';
  motDePasse = '';

  readonly collaborateurs = signal<CollaborateurResponse[]>([]);
  readonly recherche = signal('');
  readonly edition = signal(false);
  readonly selection = signal<CollaborateurResponse | null>(null);
  readonly chargementListe = signal(false);
  readonly chargement = signal(false);
  readonly erreur = signal('');
  readonly succes = signal('');

  readonly collaborateursFiltres = computed(() => {
    const terme = this.recherche().trim().toLowerCase();
    return this.collaborateurs().filter(c =>
      c.email.toLowerCase().includes(terme)
    );
  });

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargementListe.set(true);
    this.api.lister().subscribe({
      next: liste => {
        this.collaborateurs.set(liste);
        this.chargementListe.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les collaborateurs.');
        this.chargementListe.set(false);
      }
    });
  }

  ouvrirCreation(): void {
    this.email = '';
    this.motDePasse = '';
    this.erreur.set('');
    this.succes.set('');
    this.edition.set(true);
  }

  annulerCreation(): void {
    this.edition.set(false);
    this.email = '';
    this.motDePasse = '';
    this.erreur.set('');
  }

  selectionner(collaborateur: CollaborateurResponse): void {
    this.selection.set(collaborateur);
  }

  fermerFiche(): void {
    this.selection.set(null);
  }

  ajouter(): void {
    if (this.chargement()) return;

    this.erreur.set('');
    this.succes.set('');
    this.chargement.set(true);

    this.api.creer(this.email, this.motDePasse).subscribe({
      next: collaborateur => {
        this.succes.set(
          `Le collaborateur ${collaborateur.email} a été créé.`
        );
        this.collaborateurs.update(liste => [...liste, collaborateur]);
        this.email = '';
        this.motDePasse = '';
        this.edition.set(false);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set(
          'Impossible de créer le collaborateur. Vérifiez les informations.'
        );
        this.chargement.set(false);
      }
    });
  }
}
