import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
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

  async enregistrer(): Promise<void> {
    if (!this.formulaire.nom.trim() || this.enregistrement()) return;

    const modification = !!this.clientId;

    if (modification) {
      const confirmation = await Swal.fire({
        title: 'Modifier ce client ?',
        text: 'Les nouvelles informations seront enregistrées.',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Confirmer',
        cancelButtonText: 'Annuler',
        confirmButtonColor: '#6558f5',
        background: '#151d30',
        color: '#ffffff'
      });

      if (!confirmation.isConfirmed) return;
    }

    this.enregistrement.set(true);
    this.erreur.set('');

    const requete = this.clientId
      ? this.api.modifier(this.clientId, this.formulaire)
      : this.api.creer(this.formulaire);

    requete.subscribe({
      next: () => {
        this.enregistrement.set(false);
        this.edition.set(false);
        this.message.set('');
        this.charger();

        void Swal.fire({
          icon: 'success',
          title: modification ? 'Client modifié' : 'Client créé',
          text: 'Enregistrement effectué avec succès.',
          timer: 1800,
          showConfirmButton: false,
          background: '#151d30',
          color: '#ffffff'
        });
      },
      error: () => {
        this.enregistrement.set(false);

        void Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: "Impossible d'enregistrer le client.",
          confirmButtonColor: '#6558f5',
          background: '#151d30',
          color: '#ffffff'
        });
      }
    });
  }
}
