import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';

/** Demande publique de récupération, sans révéler l'existence du compte. */
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: '../login/login.scss'
})
export class ForgotPassword {
  private readonly http = inject(HttpClient);

  email = '';
  readonly chargement = signal(false);
  readonly message = signal('');
  readonly erreur = signal('');

  /** Bloque les doubles envois et conserve une réponse neutre. */
  envoyer(): void {
    if (!this.email.trim() || this.chargement()) return;

    this.chargement.set(true);
    this.erreur.set('');

    this.http.post('/api/auth/forgot-password', {
      email: this.email.trim()
    }).subscribe({
      next: () => {
        this.chargement.set(false);
        this.message.set(
          'Si un compte correspond à cette adresse, un lien sera envoyé.'
        );
      },
      error: () => {
        this.chargement.set(false);
        this.erreur.set('Service indisponible. Réessayez plus tard.');
      }
    });
  }
}
