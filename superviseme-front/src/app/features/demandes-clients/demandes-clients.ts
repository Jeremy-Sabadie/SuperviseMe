import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  DemandeClient,
  DemandeClientApiService,
  StatutDemande
} from '../../core/services/demande-client-api';

@Component({
  selector: 'app-demandes-clients',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './demandes-clients.html',
  styleUrl: './demandes-clients.scss'
})
export class DemandesClients implements OnInit {
  private readonly api = inject(DemandeClientApiService);

  readonly demandes = signal<DemandeClient[]>([]);
  readonly chargement = signal(true);
  readonly erreur = signal('');
  readonly modification = signal<string | null>(null);

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.api.lister().subscribe({
      next: demandes => {
        this.demandes.set(demandes);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les demandes.');
        this.chargement.set(false);
      }
    });
  }

  changerStatut(demande: DemandeClient, statut: StatutDemande): void {
    if (demande.statut === statut || this.modification()) return;

    this.modification.set(demande.id);
    this.erreur.set('');

    this.api.changerStatut(demande.id, statut).subscribe({
      next: resultat => {
        this.demandes.update(demandes =>
          demandes.map(d => d.id === resultat.id ? resultat : d)
        );
        this.modification.set(null);
      },
      error: () => {
        this.erreur.set('Impossible de modifier le statut.');
        this.modification.set(null);
      }
    });
  }
}
