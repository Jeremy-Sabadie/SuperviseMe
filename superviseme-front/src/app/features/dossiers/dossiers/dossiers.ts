import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { forkJoin } from 'rxjs';
import { Client, ClientApiService } from '../../../core/services/client-api';
import { CollaborateurResponse, UtilisateurApiService } from '../../../core/services/utilisateur-api';
import { DossierApiService } from '../../../core/services/dossier-api';

@Component({
  selector: 'app-dossiers',
  imports: [FormsModule, RouterLink],
  templateUrl: './dossiers.html',
  styleUrl: './dossiers.scss'
})
export class Dossiers implements OnInit {
  private readonly clientsApi = inject(ClientApiService);
  private readonly utilisateursApi = inject(UtilisateurApiService);
  private readonly dossiersApi = inject(DossierApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly clientImpose = signal<Client | null>(null);

  readonly clients = signal<Client[]>([]);
  readonly responsables = signal<CollaborateurResponse[]>([]);
  readonly chargement = signal(true);
  readonly enregistrement = signal(false);
  readonly erreur = signal('');

  formulaire = {
    titre: '',
    description: '',
    clientId: '',
    responsableId: ''
  };

  ngOnInit(): void {
    forkJoin({
      clients: this.clientsApi.lister(),
      responsables: this.utilisateursApi.lister()
    }).subscribe({
      next: ({ clients, responsables }) => {
        this.clients.set(clients);
        const clientId = this.route.snapshot.queryParamMap.get('clientId');
        const client = clients.find(c => c.id === clientId);
        if (client) {
          this.clientImpose.set(client);
          this.formulaire.clientId = client.id;
        }
        this.responsables.set(responsables);
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set(
          'Impossible de charger les clients ou les responsables. Vérifiez votre connexion administrateur.'
        );
        this.chargement.set(false);
      }
    });
  }

  enregistrer(): void {
    if (
      !this.formulaire.titre.trim() ||
      !this.formulaire.clientId ||
      !this.formulaire.responsableId ||
      this.enregistrement()
    ) return;

    this.enregistrement.set(true);

    this.dossiersApi.creer({
      titre: this.formulaire.titre.trim(),
      description: this.formulaire.description,
      clientId: this.formulaire.clientId,
      responsableId: this.formulaire.responsableId
    }).subscribe({
      next: () => {
        this.enregistrement.set(false);

        void Swal.fire({
          icon: 'success',
          title: 'Dossier créé',
          text: 'Le dossier est enregistré.',
          timer: 1600,
          showConfirmButton: false,
          background: '#151d30',
          color: '#ffffff'
        }).then(() => this.router.navigateByUrl('/'));
      },
      error: () => {
        this.enregistrement.set(false);
        void Swal.fire({
          icon: 'error',
          title: 'Création impossible',
          text: "Le dossier n'a pas été enregistré.",
          background: '#151d30',
          color: '#ffffff'
        });
      }
    });
  }
}
