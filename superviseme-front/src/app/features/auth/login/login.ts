import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthApiService } from '../../../core/services/auth-api';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthApiService);

  email = '';
  motDePasse = '';

  readonly chargement = signal(false);
  readonly erreur = signal('');

  connecter(): void {
    if (this.chargement()) return;

    this.erreur.set('');
    this.chargement.set(true);

    this.auth.connecter(this.email, this.motDePasse).subscribe({
      next: () => {
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Identifiants incorrects ou serveur indisponible.');
        this.chargement.set(false);
      }
    });
  }
}
