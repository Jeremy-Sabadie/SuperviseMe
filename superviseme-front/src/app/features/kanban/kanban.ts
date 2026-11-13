import { DemandeClientApiService } from '../../core/services/demande-client-api';
import { Component, DestroyRef, inject, OnInit, signal, computed } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, exhaustMap, forkJoin, of, timer } from 'rxjs';
import Swal from 'sweetalert2';
import { DossierApi, DossierApiService } from '../../core/services/dossier-api';
import { ClientApiService } from '../../core/services/client-api';
import { AgendaApiService, RendezVous } from '../calendar/agenda-api.service';
import { Router, RouterLink } from '@angular/router';
import { SessionService } from '../../core/auth/session';
import { AuthApiService } from '../../core/services/auth-api';
import { HttpClient } from '@angular/common/http';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';

type Statut = 'A_TRAITER' | 'EN_COURS' | 'TERMINE';

interface Dossier {
  id: string;
  titre: string;
  client: string;
  priorite: string;
  responsable: string;
  statut: string;
  echeance: string | null;
}

interface StatutConfig {
  id: string;
  libelle: string;
  couleur: string;
  ordre: number;
}

/** Statut affiché dans le Kanban, historique ou personnalisé. */
interface ColonneKanban {
  id: string;
  titre: string;
  couleur: string;
}

@Component({
  selector: 'app-kanban',
  standalone: true,
  imports: [DragDropModule, RouterLink],
  templateUrl: './kanban.html',
  styleUrl: './kanban.scss'
})
export class Kanban implements OnInit {
  readonly vueSuivi = inject(Router).url.startsWith('/suivi');

  private readonly destroyRef = inject(DestroyRef);
  private readonly demandesApi = inject(DemandeClientApiService);
  readonly demandesEnAttente = signal(0);

  private readonly session = inject(SessionService);
  private readonly dossierApi = inject(DossierApiService);
  private readonly clientApi = inject(ClientApiService);
  private readonly agendaApi = inject(AgendaApiService);
  readonly rendezVousAgenda = signal<RendezVous[]>([]);
  readonly erreurAgenda = signal(false);
  readonly chargement = signal(false);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthApiService);
  readonly estAdmin = signal(false);

  constructor() {
    if (this.session.token()) {
      this.auth.moi().subscribe({
        next: utilisateur =>
          this.estAdmin.set(utilisateur.roles.includes('ADMIN')),
        error: () => this.estAdmin.set(false)
      });
    }
  }

  deconnecter(): void {
    if (!this.session.token()) {
      this.session.deconnecter();
      void this.router.navigateByUrl('/login');
      return;
    }

    this.http.post<void>('/api/auth/logout', {}).subscribe({
      next: () => this.terminerDeconnexion(),
      error: () => this.terminerDeconnexion()
    });
  }

  private terminerDeconnexion(): void {
    this.session.deconnecter();
    void this.router.navigateByUrl('/login');
  }

  private readonly http = inject(HttpClient);

  /** Statuts historiques conservés pour les dossiers non migrés. */
  private readonly colonnesHistoriques: ColonneKanban[] = [
    { id: 'A_TRAITER', titre: 'À traiter', couleur: '#a78bfa' },
    { id: 'EN_COURS', titre: 'En cours', couleur: '#38bdf8' },
    { id: 'TERMINE', titre: 'Terminé', couleur: '#34d399' }
  ];

  readonly statutsPersonnalises = signal<StatutConfig[]>([]);

  /**
   * Affiche les étapes personnalisées et les colonnes historiques
   * contenant encore des dossiers.
   */
  readonly colonnes = computed<ColonneKanban[]>(() => {
    const anciens = this.colonnesHistoriques.filter(colonne =>
      this.dossiers().some(dossier => dossier.statut === colonne.id)
    );

    const nouveaux = this.statutsPersonnalises().map(statut => ({
      id: statut.id,
      titre: statut.libelle,
      couleur: statut.couleur
    }));

    return [...anciens, ...nouveaux];
  });

  readonly dossiers = signal<Dossier[]>([]);

  /** Dates comparées au jour local, sans décalage UTC. */
  private dateLocale(decalageJours = 0): string {
    const date = new Date();
    date.setDate(date.getDate() + decalageJours);
    const annee = date.getFullYear();
    const mois = String(date.getMonth() + 1).padStart(2, '0');
    const jour = String(date.getDate()).padStart(2, '0');
    return `${annee}-${mois}-${jour}`;
  }

  /** Dossiers non terminés dont l'échéance est dépassée. */
  readonly dossiersEnRetard = computed(() => {
    const aujourdHui = this.dateLocale();
    return this.dossiers().filter(d =>
      !!d.echeance &&
      d.echeance.slice(0, 10) < aujourdHui &&
      d.statut !== 'TERMINE'
    ).length;
  });

  /** Échéances des sept prochains jours, aujourd'hui inclus. */
  readonly prochainesEcheances = computed(() => {
    const debut = this.dateLocale();
    const fin = this.dateLocale(7);
    return this.dossiers().filter(d => {
      const date = d.echeance?.slice(0, 10);
      return !!date &&
        date >= debut &&
        date <= fin &&
        d.statut !== 'TERMINE';
    }).length;
  });


  /** Nombre de rendez-vous des sept prochains jours, aujourd'hui inclus. */
  readonly prochainsRendezVous = computed(() => {
    const maintenant = new Date();
    const debut = this.dateLocale();
    const fin = this.dateLocale(6);
    const heure = maintenant.toTimeString().slice(0, 5);

    return this.rendezVousAgenda().filter(rdv =>
      rdv.date >= debut &&
      rdv.date <= fin &&
      (rdv.date !== debut || rdv.heureFin.slice(0, 5) > heure)
    ).length;
  });

  private chargerRendezVousAgenda(): void {
    this.agendaApi.lister()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: rdv => {
          this.rendezVousAgenda.set(rdv);
          this.erreurAgenda.set(false);
        },
        error: () => this.erreurAgenda.set(true)
      });
  }

  ngOnInit(): void {
    this.chargerStatuts();
    this.chargerDossiers();
    this.chargerRendezVousAgenda();
    this.demarrerActualisationDemandes();

  }

  /**
   * Actualise le nombre de demandes à traiter toutes les 10 secondes.
   *
   * Une erreur réseau n'interrompt pas les actualisations suivantes.
   * Les requêtes ne se chevauchent pas et l'abonnement est automatiquement
   * supprimé lorsque le composant Angular est détruit.
   */
  private demarrerActualisationDemandes(): void {
    timer(0, 10_000).pipe(
      exhaustMap(() =>
        this.demandesApi.compteur().pipe(
          catchError(() => of(null))
        )
      ),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(nombre => {
      // Conserver la dernière valeur connue si le serveur est indisponible.
      if (nombre !== null) {
        this.demandesEnAttente.set(nombre);
      }
    });
  }

  /**
   * Charge les statuts des phases pré-signature et post-signature.
   * Chaque catégorie conserve son ordre de configuration.
   */
  private chargerStatuts(): void {
    const url = '/api/admin/statuts';

    forkJoin([
      this.http.get<StatutConfig[]>(url, {
        params: { categorie: 'SIGNATURE_COMMERCIALE' }
      }),
      this.http.get<StatutConfig[]>(url, {
        params: { categorie: 'APRES_SIGNATURE' }
      })
    ]).pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: ([avant, apres]) => {
        const trier = (statuts: StatutConfig[]) =>
          [...statuts].sort((a, b) => a.ordre - b.ordre);

        this.statutsPersonnalises.set([
          ...trier(avant),
          ...trier(apres)
        ]);
      },
      error: () => {
        void Swal.fire({
          icon: 'error',
          title: 'Chargement des statuts impossible',
          text: 'Les statuts personnalisés sont indisponibles.'
        });
      }
    });
  }

  chargerDossiers(): void {
    this.chargement.set(true);

    this.clientApi.lister().subscribe({
      next: clients => {
        const noms = new Map(
          clients.map(client => [
            client.id,
            client.entreprise || client.nom
          ])
        );

        this.dossierApi.lister().subscribe({
          next: dossiers => {
            this.dossiers.set(
              dossiers
                .filter(d => d.statut !== 'ARCHIVE')
                .map(d => ({
                  id: d.id,
                  titre: d.titre,
                  client: (d.clientId ? noms.get(d.clientId) : null) ?? 'Client non renseigné',
                  priorite: d.priorite === 'HAUTE'
                    ? 'Haute'
                    : d.priorite === 'BASSE' ? 'Basse' : 'Normale',
                  responsable: d.responsableId ? 'Responsable' : 'Non assigné',
                  statut: d.statutPersonnaliseId ?? d.statut,
                  echeance: d.echeance
                }))
            );
            this.chargement.set(false);
          },
          error: () => this.erreurChargement()
        });
      },
      error: () => this.erreurChargement()
    });
  }

  private erreurChargement(): void {
    this.chargement.set(false);
    void Swal.fire({
      icon: 'error',
      title: 'Chargement impossible',
      text: 'Impossible de récupérer les dossiers ou les clients.',
      background: '#151d30',
      color: '#ffffff'
    });
  }

  dossiersParStatut(statut: string): Dossier[] {
    return this.dossiers().filter(dossier => dossier.statut === statut);
  }

  deplacer(event: CdkDragDrop<Dossier[]>, statut: string): void {
    if (event.previousContainer === event.container) return;

    const dossier = event.previousContainer.data[event.previousIndex];
    if (!dossier) return;

    this.changerStatutCarte(dossier.id, statut);
  }

  /**
   * Modifie le statut depuis une carte ou un glisser-déposer.
   * Conserve la restauration du statut si l'API refuse la modification.
   */
  changerStatutCarte(dossierId: string, statut: string): void {
    const dossier = this.dossiers().find(item => item.id === dossierId);
    if (!dossier || dossier.statut === statut) return;

    const ancienStatut = dossier.statut;

    this.dossiers.update(dossiers =>
      dossiers.map(item =>
        item.id === dossier.id ? { ...item, statut } : item
      )
    );

    const estPersonnalise = this.statutsPersonnalises()
      .some(element => element.id === statut);

    const requete = estPersonnalise
      ? this.dossierApi.changerStatutPersonnalise(dossier.id, statut)
      : this.dossierApi.changerStatut(
          dossier.id,
          statut as Statut
        );

    requete.pipe(
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      error: () => {
        this.dossiers.update(dossiers =>
          dossiers.map(item =>
            item.id === dossier.id
              ? { ...item, statut: ancienStatut }
              : item
          )
        );

        void Swal.fire({
          icon: 'error',
          title: 'Déplacement impossible',
          text: 'Le statut précédent a été restauré.',
          background: '#151d30',
          color: '#ffffff'
        });
      }
    });
  }
}
