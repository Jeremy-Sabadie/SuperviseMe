import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';

import { AuthApiService } from '../../core/services/auth-api';
import {
  TacheApi,
  TacheApiService,
  StatutTache,
} from '../../core/services/tache-api';
import {
  CollaborateurResponse,
  UtilisateurApiService,
} from '../../core/services/utilisateur-api';

/** Gestion des tâches et de leurs permissions côté interface. */
@Component({
  selector: 'app-taches',
  imports: [FormsModule, RouterLink],
  templateUrl: './taches.html',
  styleUrl: './taches.scss',
})
export class Taches implements OnInit {
  private readonly api = inject(TacheApiService);
  private readonly auth = inject(AuthApiService);
  private readonly utilisateursApi = inject(UtilisateurApiService);

  readonly taches = signal<TacheApi[]>([]);
  readonly collaborateurs = signal<CollaborateurResponse[]>([]);
  readonly utilisateurId = signal('');
  readonly estAdmin = signal(false);
  readonly chargement = signal(true);
  readonly enregistrement = signal(false);
  readonly erreur = signal('');

  titre = '';
  responsableId = '';

  readonly statuts: { valeur: StatutTache; libelle: string }[] = [
    { valeur: 'NON_COMMENCEE', libelle: 'Non commencée' },
    { valeur: 'EN_COURS', libelle: 'En cours' },
    { valeur: 'TERMINEE', libelle: 'Terminée' },
  ];

  ngOnInit(): void {
    this.auth.moi().subscribe({
      next: utilisateur => {
        const admin = utilisateur.roles.includes('ADMIN');

        this.utilisateurId.set(utilisateur.id);
        this.estAdmin.set(admin);

        forkJoin({
          taches: this.api.lister(),
          collaborateurs: admin
            ? this.utilisateursApi.lister()
            : of([] as CollaborateurResponse[]),
        }).subscribe({
          next: resultat => {
            this.taches.set(resultat.taches);
            this.collaborateurs.set(
              resultat.collaborateurs.filter(c => c.actif)
            );
            this.chargement.set(false);
          },
          error: () => {
            this.erreur.set('Impossible de charger les tâches.');
            this.chargement.set(false);
          },
        });
      },
      error: () => {
        this.erreur.set('Impossible de vérifier votre session.');
        this.chargement.set(false);
      },
    });
  }

  peutModifier(tache: TacheApi): boolean {
    return this.estAdmin()
      || tache.responsableId === this.utilisateurId();
  }

  creer(): void {
    const titre = this.titre.trim();

    if (!this.estAdmin() || !titre || !this.responsableId
        || this.enregistrement()) {
      return;
    }

    this.erreur.set('');
    this.enregistrement.set(true);

    this.api.creer({
      titre,
      responsableId: this.responsableId,
      dossierId: null,
    }).subscribe({
      next: tache => {
        this.taches.update(liste => [...liste, tache]);
        this.titre = '';
        this.responsableId = '';
        this.enregistrement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de créer la tâche.');
        this.enregistrement.set(false);
      },
    });
  }

  changerStatut(tache: TacheApi, statut: StatutTache): void {
    if (!this.peutModifier(tache) || this.enregistrement()) {
      return;
    }

    this.erreur.set('');
    this.enregistrement.set(true);

    this.api.changerStatut(tache.id, statut).subscribe({
      next: resultat => {
        this.taches.update(liste =>
          liste.map(item => item.id === tache.id ? resultat : item)
        );
        this.enregistrement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de modifier le statut.');
        this.enregistrement.set(false);
      },
    });
  }

  libelleStatut(statut: StatutTache): string {
    return this.statuts.find(item => item.valeur === statut)?.libelle
      ?? statut;
  }

  responsable(tache: TacheApi): string {
    if (tache.responsableId === this.utilisateurId()) {
      return 'Moi';
    }

    return this.collaborateurs().find(
      utilisateur => utilisateur.id === tache.responsableId
    )?.email ?? 'Autre collaborateur';
  }
}
