import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Client, ClientApiService, ClientRequest } from '../../../core/services/client-api';

@Component({
  selector: 'app-clients',
  imports: [FormsModule],
  templateUrl: './clients.html',
  styleUrl: './clients.scss'
})
export class Clients implements OnInit {
  private readonly api = inject(ClientApiService);

  readonly clients = signal<Client[]>([]);
  readonly chargement = signal(false);
  readonly enregistrement = signal(false);
  readonly erreur = signal('');
  readonly message = signal('');
  readonly edition = signal(false);

  clientId: string | null = null;
  formulaire: ClientRequest = this.formulaireVide();

  ngOnInit(): void {
    this.charger();
  }

  private formulaireVide(): ClientRequest {
    return {
      nom: '',
      entreprise: '',
      email: '',
      telephone: '',
      adresse: ''
    };
  }

  charger(): void {
    this.chargement.set(true);
    this.erreur.set('');

    this.api.lister().subscribe({
      next: clients => {
        this.clients.set(clients);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les clients.');
        this.chargement.set(false);
      }
    });
  }

  nouveau(): void {
    this.clientId = null;
    this.formulaire = this.formulaireVide();
    this.erreur.set('');
    this.message.set('');
    this.edition.set(true);
  }

  modifier(client: Client): void {
    this.clientId = client.id;
    this.formulaire = {
      nom: client.nom,
      entreprise: client.entreprise ?? '',
      email: client.email ?? '',
      telephone: client.telephone ?? '',
      adresse: client.adresse ?? ''
    };
    this.erreur.set('');
    this.message.set('');
    this.edition.set(true);
  }

  annuler(): void {
    this.edition.set(false);
  }

  enregistrer(): void {
    if (!this.formulaire.nom.trim() || this.enregistrement()) return;

    this.enregistrement.set(true);
    this.erreur.set('');

    const requete = this.clientId
      ? this.api.modifier(this.clientId, this.formulaire)
      : this.api.creer(this.formulaire);

    requete.subscribe({
      next: () => {
        this.enregistrement.set(false);
        this.edition.set(false);
        this.message.set('Client enregistré avec succès.');
        this.charger();
      },
      error: () => {
        this.enregistrement.set(false);
        this.erreur.set("Impossible d'enregistrer le client.");
      }
    });
  }
}
