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

  readonly confirmationDesactivation = signal(false);
  readonly modificationStatut = signal(false);

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
    // Une confirmation ouverte ne doit jamais concerner une autre fiche.
    this.confirmationDesactivation.set(false);
    this.selection.set(collaborateur);
  }

  fermerFiche(): void {
    this.confirmationDesactivation.set(false);
    this.selection.set(null);
  }

  /** Ouvre une confirmation sans modifier le compte. */
  demanderDesactivation(): void {
    if (!this.selection()?.actif || this.selection()?.role === 'ADMIN' || this.modificationStatut()) return;
    this.confirmationDesactivation.set(true);
  }

  /** Annuler ne déclenche aucun appel au serveur. */
  annulerDesactivation(): void {
    this.confirmationDesactivation.set(false);
  }

  /** La désactivation n'est appliquée qu'après confirmation du serveur. */
  confirmerDesactivation(): void {
    const collaborateur = this.selection();
    if (!collaborateur?.actif || collaborateur.role === 'ADMIN' || !this.confirmationDesactivation()
        || this.modificationStatut()) return;

    this.modificationStatut.set(true);
    this.erreur.set('');
    this.confirmationDesactivation.set(false);

    this.api.desactiver(collaborateur.id).subscribe({
      next: resultat => {
        this.actualiserCollaborateur(resultat);
        this.succes.set('Le collaborateur a été désactivé.');
        this.modificationStatut.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de désactiver ce collaborateur.');
        this.modificationStatut.set(false);
      }
    });
  }

  /** Réactive le compte sans recréer le collaborateur. */
  reactiverSelection(): void {
    const collaborateur = this.selection();
    if (!collaborateur || collaborateur.actif || this.modificationStatut()) return;

    this.modificationStatut.set(true);
    this.erreur.set('');

    this.api.reactiver(collaborateur.id).subscribe({
      next: resultat => {
        this.actualiserCollaborateur(resultat);
        this.succes.set('Le collaborateur a été réactivé.');
        this.modificationStatut.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de réactiver ce collaborateur.');
        this.modificationStatut.set(false);
      }
    });
  }

  /** Conserve la sélection et remplace uniquement les données actualisées. */
  private actualiserCollaborateur(resultat: CollaborateurResponse): void {
    this.collaborateurs.update(liste =>
      liste.map(c => c.id === resultat.id ? resultat : c)
    );

    if (this.selection()?.id === resultat.id) {
      this.selection.set(resultat);
    }
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
