import Swal from 'sweetalert2';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, of, switchMap } from 'rxjs';
import { StatutPersonnaliseApiService } from '../../../core/services/statut-personnalise-api';
import { StatutPersonnalise } from '../../../core/services/statut-personnalise.model';
import { TacheApi, TacheApiService, StatutTache } from '../../../core/services/tache-api';
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
  private readonly statutsApi = inject(StatutPersonnaliseApiService);
  readonly statutsPersonnalises = signal<StatutPersonnalise[]>([]);
  statutSelectionne = '';

  couleurStatutDossier(dossier: DossierApi): string {
    return this.statutsPersonnalises()
      .find(s => s.id === dossier.statutPersonnaliseId)?.couleur ?? '#65c7d0';
  }

  libelleStatutDossier(dossier: DossierApi): string {
    const statut = this.statutsPersonnalises()
      .find(s => s.id === dossier.statutPersonnaliseId);

    return statut?.libelle ?? dossier.statut.replaceAll('_', ' ');
  }
  private readonly clientsApi = inject(ClientApiService);
  private readonly utilisateursApi = inject(UtilisateurApiService);
  private readonly authApi = inject(AuthApiService);
  private readonly tachesApi = inject(TacheApiService);

  readonly tachesDossier = signal<TacheApi[]>([]);
  readonly erreurTaches = signal('');
  readonly enregistrementTache = signal(false);
  formulaireTacheOuvert = false;
  colonneMobile: StatutTache = 'NON_COMMENCEE';

  readonly colonnesTaches: { statut: StatutTache; libelle: string }[] = [
    { statut: 'NON_COMMENCEE', libelle: 'À faire' },
    { statut: 'EN_COURS', libelle: 'En cours' },
    { statut: 'TERMINEE', libelle: 'Terminées' }
  ];

  nombreTaches(statut: StatutTache): number {
    return this.tachesDossier().filter(t => t.statut === statut).length;
  }

  titreTache = '';
  responsableTacheId = '';

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

  modeEdition = false;

  avancement(): number {
    const taches = this.tachesDossier();
    if (!taches.length) return 0;
    return Math.round(
      100 * taches.filter(t => t.statut === 'TERMINEE').length / taches.length
    );
  }

  ngOnInit(): void {
    this.modeEdition =
      this.route.snapshot.queryParamMap.get('mode') === 'edition';

    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.erreur.set('Identifiant du dossier manquant.');
      this.chargement.set(false);
      return;
    }

    // Identifier le rôle avant de demander les données protégées.
    // L'annuaire complet est exclusivement accessible aux ADMIN.
    this.authApi.moi().pipe(
      switchMap(utilisateur => {
        const estAdmin = utilisateur.roles.includes('ADMIN');

        return forkJoin({
          dossier: this.api.consulter(id),
          clients: this.clientsApi.lister(),
          responsables: estAdmin
            ? this.utilisateursApi.lister()
            : of([] as CollaborateurResponse[]),
          statutsAvant: this.statutsApi.lister('SIGNATURE_COMMERCIALE'),
          statutsApres: this.statutsApi.lister('APRES_SIGNATURE')
        }).pipe(
          switchMap(donnees => of({ ...donnees, utilisateur }))
        );
      })
    ).subscribe({
      next: ({ dossier, clients, responsables, utilisateur, statutsAvant, statutsApres }) => {
      this.statutsPersonnalises.set(
        [...statutsAvant, ...statutsApres].sort((a, b) => a.ordre - b.ordre)
      );
      this.statutSelectionne = dossier.statutPersonnaliseId ?? dossier.statut;
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
        this.chargerTaches(dossier.id);
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
    // Ne jamais ajouter un compte désactivé, même par appel direct de la méthode.
    if (selectionne && !this.responsables().some(c => c.id === id && c.actif)) return;
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
    // Priorité à l'annuaire lorsqu'il est accessible (ADMIN).
    const responsable = this.responsables().find(r => r.id === id);

    // Pour un USER, l'API du dossier fournit l'identité minimale
    // du responsable sans exposer l'annuaire complet.
    if (!responsable) {
      const dossier = this.dossier();
      if (dossier?.responsableId === id && dossier.responsableNom) {
        return dossier.responsableNom;
      }

      return 'Responsable indisponible';
    }
    return responsable.prenom && responsable.nom
      ? `${responsable.prenom} ${responsable.nom}`
      : 'Identité à renseigner';
  }

  /** Détecte les modifications des informations ou du statut du dossier. */
  estModifie(): boolean {
    const original = this.dossier();
    if (!original) return false;

    return this.informationsModifiees(original)
      || this.statutSelectionne !== (original.statutPersonnaliseId ?? original.statut);
  }

  /** Compare les informations indépendamment du statut personnalisé. */
  private informationsModifiees(original: DossierApi): boolean {
    return this.formulaire.titre.trim() !== original.titre
      || this.formulaire.description !== (original.description ?? '')
      || this.formulaire.priorite !== original.priorite
      || this.formulaire.echeance !== (original.echeance ?? '')
      || this.formulaire.clientId !== (original.clientId ?? '');
  }

  /** Enregistre les informations puis, si nécessaire, le statut sélectionné. */
  async enregistrer(): Promise<void> {
    const dossier = this.dossier();
    if (!dossier || !this.formulaire.titre.trim()
      || !this.estModifie() || this.enregistrement()) return;

    const confirmation = await Swal.fire({
      title: 'Modifier ce dossier ?',
      text: 'Les modifications seront enregistrées.',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Confirmer',
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#4f9ca5',
      cancelButtonColor: '#344854',
      background: '#151d30',
      color: '#ffffff',
      reverseButtons: true
    });

    if (!confirmation.isConfirmed || this.enregistrement()) return;

    const statutChange =
      this.statutSelectionne !== (dossier.statutPersonnaliseId ?? dossier.statut);

    const informationsChangees = this.informationsModifiees(dossier);

    this.enregistrement.set(true);
    this.erreur.set('');
    this.succes.set(false);

    const sauvegarde = informationsChangees
      ? this.api.modifier(dossier.id, {
          titre: this.formulaire.titre.trim(),
          description: this.formulaire.description,
          statut: dossier.statut,
          priorite: this.formulaire.priorite,
          echeance: this.formulaire.echeance || null,
          clientId: this.formulaire.clientId || null
        })
      : of(dossier);

    sauvegarde.pipe(
      switchMap(resultat => {
        if (!statutChange) return of(resultat);

        const statutPersonnalise = this.statutsPersonnalises()
          .find(statut => statut.id === this.statutSelectionne);

        if (statutPersonnalise) {
          return this.api.changerStatutPersonnalise(
            dossier.id, statutPersonnalise.id
          );
        }

        if (this.statutSelectionne === dossier.statut) {
          return this.api.changerStatut(dossier.id, dossier.statut);
        }

        throw new Error('Statut sélectionné non reconnu.');
      })
    ).subscribe({
      next: resultat => {
        this.dossier.set(resultat);
        this.formulaire.statut = resultat.statut;
        this.statutSelectionne =
          resultat.statutPersonnaliseId ?? resultat.statut;
        this.enregistrement.set(false);
        this.succes.set(true);

        void Swal.fire({
          title: 'Dossier modifié avec succès',
          icon: 'success',
          timer: 1800,
          showConfirmButton: false,
          background: '#151d30',
          color: '#ffffff'
        });
      },
      error: () => {
        this.enregistrement.set(false);
        this.erreur.set('Modification impossible. Vérifiez le statut et vos droits.');

        void Swal.fire({
          title: 'Modification impossible',
          text: 'Vérifiez le statut sélectionné et vos droits.',
          icon: 'error',
          confirmButtonColor: '#4f9ca5',
          background: '#151d30',
          color: '#ffffff'
        });
      }
    });
  }

  chargerTaches(dossierId: string): void {
    this.tachesApi.lister().subscribe({
      next: taches => {
        this.tachesDossier.set(taches.filter(t => t.dossierId === dossierId));
        this.erreurTaches.set('');
      },
      error: () => this.erreurTaches.set('Impossible de charger les tâches du dossier.')
    });
  }

  async creerTache(): Promise<void> {
    const dossier = this.dossier();
    const titre = this.titreTache.trim();
    if (!dossier || !this.estAdmin() || !titre || !this.responsableTacheId
        || this.enregistrementTache()) return;

    const confirmation = await Swal.fire({
      title: 'Créer cette tâche ?',
      text: `Ajouter « ${titre} » au dossier « ${dossier.titre} » ?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Créer',
      cancelButtonText: 'Annuler',
      background: '#1b2a35',
      color: '#e5eef2',
      confirmButtonColor: '#4f9ca5',
      cancelButtonColor: '#344854',
      reverseButtons: true
    });
    if (!confirmation.isConfirmed || this.enregistrementTache()) return;

    this.enregistrementTache.set(true);
    this.erreurTaches.set('');

    this.tachesApi.creer({
      titre,
      responsableId: this.responsableTacheId,
      dossierId: dossier.id
    }).subscribe({
      next: tache => {
        this.tachesDossier.update(liste => [...liste, tache]);
        this.titreTache = '';
        this.responsableTacheId = '';
        this.formulaireTacheOuvert = false;
        this.enregistrementTache.set(false);
        void Swal.fire({
          title: 'Tâche créée',
          icon: 'success',
          timer: 1500,
          showConfirmButton: false,
          background: '#1b2a35',
          color: '#e5eef2'
        });
      },
      error: () => {
        this.erreurTaches.set('Impossible de créer la tâche.');
        this.enregistrementTache.set(false);
        void Swal.fire({
          title: 'Création impossible',
          icon: 'error',
          background: '#1b2a35',
          color: '#e5eef2'
        });
      }
    });
  }

  async ouvrirStatutTache(tache: TacheApi): Promise<void> {
    if (!this.estAdmin() || this.enregistrementTache()) return;

    const choix = await Swal.fire({
      title: tache.titre,
      text: 'Choisissez le nouveau statut.',
      input: 'select',
      inputOptions: {
        NON_COMMENCEE: 'À faire',
        EN_COURS: 'En cours',
        TERMINEE: 'Terminée'
      },
      inputValue: tache.statut,
      showCancelButton: true,
      confirmButtonText: 'Continuer',
      cancelButtonText: 'Annuler',
      background: '#1b2a35',
      color: '#e5eef2',
      confirmButtonColor: '#4f9ca5',
      cancelButtonColor: '#344854',
      reverseButtons: true
    });

    if (!choix.isConfirmed || !choix.value
        || choix.value === tache.statut) return;

    const statuts: StatutTache[] = [
      'NON_COMMENCEE', 'EN_COURS', 'TERMINEE'
    ];
    if (!statuts.includes(choix.value as StatutTache)) return;

    await this.changerStatutTache(tache, choix.value as StatutTache);
  }

  async changerStatutTache(tache: TacheApi, statut: StatutTache): Promise<void> {
    if (!this.estAdmin() || this.enregistrementTache()
        || tache.statut === statut) return;

    const confirmation = await Swal.fire({
      title: 'Modifier le statut ?',
      text: `Confirmer la modification de « ${tache.titre} » ?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Confirmer',
      cancelButtonText: 'Annuler',
      background: '#1b2a35',
      color: '#e5eef2',
      confirmButtonColor: '#4f9ca5',
      cancelButtonColor: '#344854',
      reverseButtons: true
    });
    if (!confirmation.isConfirmed || this.enregistrementTache()) return;

    this.enregistrementTache.set(true);
    this.erreurTaches.set('');

    this.tachesApi.changerStatut(tache.id, statut).subscribe({
      next: resultat => {
        this.tachesDossier.update(liste =>
          liste.map(item => item.id === tache.id ? resultat : item)
        );
        this.enregistrementTache.set(false);
        void Swal.fire({
          title: 'Statut mis à jour',
          icon: 'success',
          timer: 1500,
          showConfirmButton: false,
          background: '#1b2a35',
          color: '#e5eef2'
        });
      },
      error: () => {
        this.erreurTaches.set('Impossible de modifier le statut.');
        this.enregistrementTache.set(false);
        void Swal.fire({
          title: 'Modification impossible',
          icon: 'error',
          background: '#1b2a35',
          color: '#e5eef2'
        });
      }
    });
  }

}
