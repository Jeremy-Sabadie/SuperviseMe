import Swal from 'sweetalert2';
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
    this.selection.set(collaborateur);
  }

  fermerFiche(): void {
    this.selection.set(null);
  }

  /** Confirmation explicite avant toute désactivation. */
  async demanderDesactivation(): Promise<void> {
    const collaborateur = this.selection();
    if (!collaborateur?.actif || collaborateur.role === 'ADMIN'
        || this.modificationStatut()) return;

    const confirmation = await Swal.fire({
      title: 'Désactiver ce collaborateur ?',
      text: `${collaborateur.email} perdra immédiatement son accès. Ses dossiers et son historique seront conservés.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Désactiver',
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#dc3545',
      background: '#151d30',
      color: '#ffffff',
      width: 'min(420px, 95vw)',
      reverseButtons: true
    });

    if (!confirmation.isConfirmed || this.selection()?.id !== collaborateur.id
        || this.modificationStatut()) return;

    this.modificationStatut.set(true);
    this.erreur.set('');

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
