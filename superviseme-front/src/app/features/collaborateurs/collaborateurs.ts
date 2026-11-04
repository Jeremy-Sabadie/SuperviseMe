import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UtilisateurApiService } from '../../core/services/utilisateur-api';

@Component({
  selector: 'app-collaborateurs',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './collaborateurs.html',
  styleUrl: './collaborateurs.scss'
})
export class Collaborateurs {
  private readonly api = inject(UtilisateurApiService);

  email = '';
  motDePasse = '';

  readonly chargement = signal(false);
  readonly erreur = signal('');
  readonly succes = signal('');

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
        this.email = '';
        this.motDePasse = '';
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
