import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthApiService } from '../../../core/services/auth-api';
import { Client, ClientApiService } from '../../../core/services/client-api';
import { CollaborateurResponse, UtilisateurApiService } from '../../../core/services/utilisateur-api';
import { DossierApi, DossierApiService, PrioriteDossier, StatutDossier } from '../../../core/services/dossier-api';

@Component({
  selector: 'app-fiche-dossier',
  imports: [FormsModule, RouterLink],
  templateUrl: './fiche-dossier.html',
  styleUrl: './fiche-dossier.scss'
})
export class FicheDossier implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(DossierApiService);
  private readonly clientsApi = inject(ClientApiService);
  private readonly utilisateursApi = inject(UtilisateurApiService);
  private readonly authApi = inject(AuthApiService);

  readonly clients = signal<Client[]>([]);
  readonly responsables = signal<CollaborateurResponse[]>([]);
  readonly estAdmin = signal(false);
  readonly participantsSelectionnes = signal<string[]>([]);
  readonly enregistrementParticipants = signal(false);
  readonly succesParticipants = signal(false);

  readonly dossier = signal<DossierApi | null>(null);
  readonly erreur = signal('');
  readonly chargement = signal(true);
  readonly enregistrement = signal(false);
  readonly succes = signal(false);

  formulaire = {
    titre: '',
    description: '',
    statut: 'A_TRAITER' as StatutDossier,
    priorite: 'NORMALE' as PrioriteDossier,
    echeance: '',
    clientId: ''
  };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.erreur.set('Identifiant du dossier manquant.');
      this.chargement.set(false);
      return;
    }

    forkJoin({
      dossier: this.api.consulter(id),
      clients: this.clientsApi.lister(),
      responsables: this.utilisateursApi.lister(),
      utilisateur: this.authApi.moi()
    }).subscribe({
      next: ({ dossier, clients, responsables, utilisateur }) => {
        this.clients.set(clients);
        this.responsables.set(responsables);
        this.estAdmin.set(utilisateur.roles.includes('ADMIN'));
        this.participantsSelectionnes.set([...dossier.participantIds]);
        this.dossier.set(dossier);
        this.formulaire = {
          titre: dossier.titre,
          description: dossier.description ?? '',
          statut: dossier.statut,
          priorite: dossier.priorite,
          echeance: dossier.echeance ?? '',
          clientId: dossier.clientId ?? ''
        };
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger ce dossier.');
        this.chargement.set(false);
      }
    });
  }

  participantSelectionne(id: string): boolean {
    return this.participantsSelectionnes().includes(id);
  }

  basculerParticipant(id: string, selectionne: boolean): void {
    this.participantsSelectionnes.update(ids =>
      selectionne ? [...ids.filter(item => item !== id), id] : ids.filter(item => item !== id)
    );
    this.succesParticipants.set(false);
  }

  participantsModifies(): boolean {
    const dossier = this.dossier();
    if (!dossier) return false;

    const selection = this.participantsSelectionnes();
    return selection.length !== dossier.participantIds.length
      || selection.some(id => !dossier.participantIds.includes(id));
  }

  enregistrerParticipants(): void {
    const dossier = this.dossier();
    if (!dossier || !this.estAdmin() || !this.participantsModifies()
        || this.enregistrementParticipants()) return;

    this.enregistrementParticipants.set(true);
    this.erreur.set('');
    this.succesParticipants.set(false);

    this.api.affecterParticipants(dossier.id, this.participantsSelectionnes()).subscribe({
      next: resultat => {
        this.dossier.set(resultat);
        this.participantsSelectionnes.set([...resultat.participantIds]);
        this.enregistrementParticipants.set(false);
        this.succesParticipants.set(true);
      },
      error: () => {
        this.enregistrementParticipants.set(false);
        this.erreur.set("Impossible d'attribuer les participants.");
      }
    });
  }

  nomClient(id: string | null): string {
    if (!id) return 'Non renseigné';
    const client = this.clients().find(c => c.id === id);
    return client ? (client.entreprise || client.nom) : 'Client indisponible';
  }

  nomResponsable(id: string | null): string {
    if (!id) return 'Non assigné';
    return this.responsables().find(r => r.id === id)?.email
      ?? 'Responsable indisponible';
  }

  estModifie(): boolean {
    const original = this.dossier();
    if (!original) return false;

    return this.formulaire.titre.trim() !== original.titre
      || this.formulaire.description !== (original.description ?? '')
      || this.formulaire.statut !== original.statut
      || this.formulaire.priorite !== original.priorite
      || this.formulaire.echeance !== (original.echeance ?? '')
      || this.formulaire.clientId !== (original.clientId ?? '');
  }

  enregistrer(): void {
    const dossier = this.dossier();
    if (!dossier || !this.formulaire.titre.trim() || !this.estModifie() || this.enregistrement()) return;

    this.enregistrement.set(true);
    this.erreur.set('');
    this.succes.set(false);

    this.api.modifier(dossier.id, {
      titre: this.formulaire.titre.trim(),
      description: this.formulaire.description,
      statut: this.formulaire.statut,
      priorite: this.formulaire.priorite,
      echeance: this.formulaire.echeance || null,
      clientId: this.formulaire.clientId || null
    }).subscribe({
      next: resultat => {
        this.dossier.set(resultat);
        this.enregistrement.set(false);
        this.succes.set(true);
      },
      error: () => {
        this.enregistrement.set(false);
        this.erreur.set('Modification impossible. Vérifiez vos droits.');
      }
    });
  }
}
