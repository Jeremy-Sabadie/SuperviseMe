import Swal from 'sweetalert2';

import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

interface Realisation {
  id: string;
  titre: string;
  description: string | null;
  imageUrl: string;
  position: number;
}

@Component({
  selector: 'app-formulaire-admin',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './formulaire-admin.html',
  styleUrl: './formulaire-admin.scss'
})
export class FormulaireAdmin implements OnInit {
  private readonly http = inject(HttpClient);

  readonly lienPublic = new URL('/contact', window.location.origin).href;
  readonly lienCopie = signal(false);

  async copierLien(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.lienPublic);
      this.lienCopie.set(true);
      this.erreur.set('');
    } catch {
      this.erreur.set('Copie impossible. Sélectionnez le lien manuellement.');
    }
  }

  readonly onglet = signal<'realisations' | 'apercu'>('realisations');
  readonly realisations = signal<Realisation[]>([]);
  readonly chargement = signal(false);
  readonly erreur = signal('');
  readonly succes = signal('');

  titre = '';
  description = '';
  fichier: File | null = null;

  ngOnInit(): void { this.actualiser(); }

  actualiser(): void {
    this.http.get<Realisation[]>('/api/public/realisations').subscribe({
      next: data => this.realisations.set(data),
      error: () => this.erreur.set('Impossible de charger les réalisations.')
    });
  }

  choisirFichier(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fichier = input.files?.[0] ?? null;
  }

  async ajouter(): Promise<void> {
    if (!this.fichier || !this.titre.trim() || this.chargement()) return;

    if (!['image/jpeg', 'image/png'].includes(this.fichier.type) ||
        this.fichier.size > 5 * 1024 * 1024) {
      await Swal.fire({
        icon: 'warning',
        title: 'Photo non valide',
        text: 'Choisissez une image JPEG ou PNG de 5 Mo maximum.',
        confirmButtonColor: '#7060d2'
      });
      return;
    }

    const confirmation = await Swal.fire({
      icon: 'question',
      title: 'Publier cette réalisation ?',
      text: 'La photo sera immédiatement visible sur votre formulaire public.',
      showCancelButton: true,
      confirmButtonText: 'Oui, publier',
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#7060d2',
      cancelButtonColor: '#475569',
      reverseButtons: true
    });

    if (!confirmation.isConfirmed) return;

    const data = new FormData();
    data.append('titre', this.titre.trim());
    data.append('description', this.description.trim());
    data.append('fichier', this.fichier);

    this.chargement.set(true);
    this.erreur.set('');
    this.succes.set('');

    this.http.post('/api/realisations', data).subscribe({
      next: async () => {
        this.chargement.set(false);
        this.titre = '';
        this.description = '';
        this.fichier = null;
        this.actualiser();

        await Swal.fire({
          icon: 'success',
          title: 'Réalisation publiée !',
          text: 'Votre photo est désormais visible sur le formulaire de contact.',
          confirmButtonText: 'Terminer',
          confirmButtonColor: '#7060d2'
        });
      },
      error: async (err) => {
        this.chargement.set(false);

        await Swal.fire({
          icon: 'error',
          title: 'Publication impossible',
          text: `HTTP ${err.status} : ${err.error?.message || err.message || "Erreur inconnue"}`,
          confirmButtonColor: '#7060d2'
        });
      }
    });
  }

  modifier(item: Realisation): void {
    this.erreur.set('');
    this.http.put('/api/realisations/' + item.id, {
      titre: item.titre,
      description: item.description,
      position: item.position
    }).subscribe({
      next: () => {
        this.succes.set('Modifications enregistrées.');
        this.actualiser();
      },
      error: () => this.erreur.set('Modification impossible.')
    });
  }

  supprimer(item: Realisation): void {
    if (!confirm('Supprimer définitivement cette réalisation ?')) return;

    this.http.delete('/api/realisations/' + item.id).subscribe({
      next: () => {
        this.succes.set('Réalisation supprimée.');
        this.actualiser();
      },
      error: () => this.erreur.set('Suppression impossible.')
    });
  }
}
