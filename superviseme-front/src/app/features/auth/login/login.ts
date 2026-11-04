import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthApiService } from '../../../core/services/auth-api';
import { SessionService } from '../../../core/auth/session';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthApiService);
  private readonly session = inject(SessionService);
  private readonly router = inject(Router);

  email = '';
  motDePasse = '';

  readonly chargement = signal(false);
  readonly erreur = signal('');

  connecter(): void {
    if (this.chargement()) return;

    this.erreur.set('');
    this.chargement.set(true);

    this.auth.connecter(this.email, this.motDePasse).subscribe({
      next: ({ accessToken }) => {
        this.session.definirToken(accessToken);
        this.chargement.set(false);
        void this.router.navigateByUrl('/');
      },
      error: () => {
        this.erreur.set('Identifiants incorrects ou serveur indisponible.');
        this.chargement.set(false);
      }
    });
  }
}
