import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';

/**
 * Réinitialisation avec jeton temporaire.
 * Seul le backend décide de la validité du jeton.
 */
@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: '../login/login.scss'
})
export class ResetPassword {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);

  readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';
  readonly chargement = signal(false);
  readonly termine = signal(false);
  readonly erreur = signal('');

  motDePasse = '';
  confirmation = '';

  /** Vérifie la saisie avant l'appel sécurisé à l'API. */
  reinitialiser(): void {
    if (!this.token || this.chargement()) return;

    if (this.motDePasse.length < 12 ||
        this.motDePasse.length > 128 ||
        this.motDePasse !== this.confirmation) {
      this.erreur.set('Vérifiez le mot de passe et sa confirmation (12 à 128 caractères).');
      return;
    }

    this.chargement.set(true);
    this.erreur.set('');

    this.http.post('/api/auth/reset-password', {
      token: this.token,
      motDePasse: this.motDePasse
    }).subscribe({
      next: () => {
        // Ne pas conserver le nouveau mot de passe dans le composant.
        this.motDePasse = '';
        this.confirmation = '';
        this.chargement.set(false);
        this.termine.set(true);
      },
      error: () => {
        this.chargement.set(false);
        this.erreur.set('Lien invalide ou expiré. Demandez un nouveau lien.');
      }
    });
  }
}
