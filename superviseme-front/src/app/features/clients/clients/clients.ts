import Swal from 'sweetalert2';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { DossierApi, DossierApiService } from '../../../core/services/dossier-api';
import { Client, ClientApiService, ClientRequest } from '../../../core/services/client-api';

@Component({
  selector: 'app-clients',
  imports: [FormsModule, RouterLink],
  templateUrl: './clients.html',
  styleUrl: './clients.scss'
})
export class Clients implements OnInit {
  private readonly api = inject(ClientApiService);
  private readonly dossiersApi = inject(DossierApiService);

  readonly clients = signal<Client[]>([]);
  readonly dossiers = signal<DossierApi[]>([]);
  readonly clientSelectionne = signal<Client | null>(null);
  readonly dossiersClient = computed(() =>
    this.dossiers().filter(d =>
      d.clientId === this.clientSelectionne()?.id
    )
  );
  readonly recherche = signal('');
  readonly clientsFiltres = computed(() => {
    const terme = this.recherche().trim().toLocaleLowerCase();
    return this.clients().filter(client =>
      [client.nom, client.entreprise, client.email, client.telephone]
        .some(valeur => (valeur ?? '').toLocaleLowerCase().includes(terme))
    );
  });
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

    forkJoin({
      clients: this.api.lister(),
      dossiers: this.dossiersApi.lister()
    }).subscribe({
      next: ({ clients, dossiers }) => {
        this.clients.set(clients);
        this.dossiers.set(dossiers);
        const id = this.clientSelectionne()?.id;
        this.clientSelectionne.set(
          clients.find(c => c.id === id) ?? null
        );
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger les clients.');
        this.chargement.set(false);
      }
    });
  }

  selectionner(client: Client): void {
    this.clientSelectionne.set(client);
    this.edition.set(false);
  }

  fermerFiche(): void {
    this.clientSelectionne.set(null);
    this.edition.set(false);
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
