import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import {
  AuthApiService,
  ProfilUtilisateur
} from '../../core/services/auth-api';
import { SessionService } from '../../core/auth/session';

/**
 * Présentation et interactions du profil.
 *
 * Les validations métier restent côté API.
 * Aucun changement de rôle ou de statut n'est possible ici.
 */
@Component({
  selector: 'app-profil',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './profil.html',
  styleUrl: './profil.scss'
})
export class Profil implements OnInit {
  private readonly api = inject(AuthApiService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  readonly profil = signal<ProfilUtilisateur | null>(null);
  readonly chargement = signal(true);
  readonly enregistrement = signal(false);
  readonly erreur = signal('');
  readonly succes = signal('');

  editionIdentite = false;
  editionEmail = false;
  editionMotDePasse = false;

  prenom = '';
  nom = '';
  email = '';

  motDePasseEmail = '';
  motDePasseActuel = '';
  nouveauMotDePasse = '';
  confirmation = '';

  ngOnInit(): void {
    this.api.profil().subscribe({
      next: compte => {
        this.actualiserProfil(compte);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger votre profil.');
        this.chargement.set(false);
      }
    });
  }

  /**
   * Centralise la mise à jour des données affichées
   * et des valeurs initiales des formulaires.
   */
  private actualiserProfil(compte: ProfilUtilisateur): void {
    this.profil.set(compte);
    this.prenom = compte.prenom ?? '';
    this.nom = compte.nom ?? '';
    this.email = compte.email;
  }

  private commencerEnregistrement(): void {
    this.erreur.set('');
    this.succes.set('');
    this.enregistrement.set(true);
  }

  private terminerAvecErreur(): void {
    this.erreur.set(
      'Enregistrement impossible. Vérifiez les informations saisies.'
    );
    this.enregistrement.set(false);
  }

  /** Les champs facultatifs peuvent rester vides. */
  enregistrerIdentite(): void {
    if (this.prenom.trim().length > 100 ||
        this.nom.trim().length > 100) {
      this.erreur.set('100 caractères maximum par champ.');
      return;
    }

    this.commencerEnregistrement();

    this.api.modifierIdentite(
      this.prenom.trim(),
      this.nom.trim()
    ).subscribe({
      next: compte => {
        this.actualiserProfil(compte);
        this.editionIdentite = false;
        this.enregistrement.set(false);
        this.succes.set('Informations enregistrées.');
      },
      error: () => this.terminerAvecErreur()
    });
  }

  /** Une modification d'e-mail impose une reconnexion. */
  enregistrerEmail(): void {
    if (!this.email.trim() || !this.motDePasseEmail) {
      this.erreur.set('E-mail et mot de passe actuel requis.');
      return;
    }

    this.commencerEnregistrement();

    this.api.modifierEmail(
      this.email.trim(),
      this.motDePasseEmail
    ).subscribe({
      next: () => this.reconnecter(),
      error: () => this.terminerAvecErreur()
    });
  }

  /** Vérifie la confirmation avant l'appel réseau. */
  enregistrerMotDePasse(): void {
    if (!this.motDePasseActuel ||
        this.nouveauMotDePasse.length < 12 ||
        this.nouveauMotDePasse.length > 128 ||
        this.nouveauMotDePasse !== this.confirmation) {
      this.erreur.set(
        'Mot de passe invalide ou confirmation différente (12 à 128 caractères).'
      );
      return;
    }

    this.commencerEnregistrement();

    this.api.modifierMotDePasse(
      this.motDePasseActuel,
      this.nouveauMotDePasse
    ).subscribe({
      next: () => this.reconnecter(),
      error: () => this.terminerAvecErreur()
    });
  }

  /** Efface les données de session après modification sensible. */
  private reconnecter(): void {
    this.motDePasseEmail = '';
    this.motDePasseActuel = '';
    this.nouveauMotDePasse = '';
    this.confirmation = '';

    this.session.deconnecter();
    void this.router.navigate(['/login']);
  }

  annulerIdentite(): void {
    const compte = this.profil();
    if (compte) this.actualiserProfil(compte);
    this.editionIdentite = false;
  }

  annulerEmail(): void {
    this.email = this.profil()?.email ?? '';
    this.motDePasseEmail = '';
    this.editionEmail = false;
  }
}
