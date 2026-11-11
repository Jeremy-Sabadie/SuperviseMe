import Swal from 'sweetalert2';
import { detecterConflits } from '../utils/conflits';
import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { FormsModule } from '@angular/forms';
import { AuthApiService } from '../../../core/services/auth-api';
import { DossierApiService } from '../../../core/services/dossier-api';
import {
  EcheanceAgenda,
  extraireEcheances
} from '../utils/echeances';
import {
  AgendaApiService,
  RendezVous,
  RendezVousRequest
} from '../agenda-api.service';

registerLocaleData(localeFr);
import {
  ajouterMois,
  decalagePremierJour,
  joursDuMois
} from '../utils/calendrier';

/** Vues disponibles dans l'Agenda. */
type VueAgenda = 'mois' | 'semaine' | 'jour';

/** Affichage et navigation du calendrier. */
@Component({
  selector: 'app-calendar',
  imports: [DatePipe, FormsModule],
  templateUrl: './calendar.html',
  styleUrl: './calendar.scss'
})
export class Calendar {
  private readonly agendaApi = inject(AgendaApiService);
  private readonly authApi = inject(AuthApiService);
  private readonly dossierApi = inject(DossierApiService);

  /** Seul ADMIN peut modifier les rendez-vous. */
  readonly estAdmin = signal(false);

  /** Échéances des dossiers actifs, sans horaire. */
  readonly echeances = signal<EcheanceAgenda[]>([]);

  /** Échéances associées à une date civile. */
  echeancesDuJour(date: Date): EcheanceAgenda[] {
    const cle = this.formulaireInitial(date).date;
    return this.echeances().filter(echeance => echeance.date === cle);
  }

  /** Trois prochaines échéances de dossiers. */
  readonly prochainesEcheances = computed(() => {
    const aujourdHui = this.formulaireInitial(new Date()).date;

    return [...this.echeances()]
      .filter(echeance => echeance.date >= aujourdHui)
      .sort((a, b) => a.date.localeCompare(b.date))
      .slice(0, 3);
  });

  /** Charge les échéances et applique le filtrage métier existant. */
  private chargerEcheances(): void {
    this.dossierApi.lister().subscribe({
      next: dossiers => this.echeances.set(extraireEcheances(dossiers)),
      error: () => this.erreur.set('Chargement des échéances impossible.')
    });
  }

  /** Rendez-vous chargés depuis PostgreSQL via l'API. */
  readonly rendezVous = signal<RendezVous[]>([]);

  /** Identifiant renseigné uniquement lors d'une modification. */
  readonly editionId = signal<string | null>(null);

  /** Message d'erreur présenté sans masquer le formulaire. */
  readonly erreur = signal('');

  /** Évite de soumettre deux fois un rendez-vous. */
  readonly enregistrement = signal(false);

  formulaire: RendezVousRequest = this.formulaireInitial(new Date());

  constructor() {
    this.chargerRendezVous();
    this.chargerEcheances();

    // Les permissions sont déterminées par le serveur.
    this.authApi.moi().subscribe({
      next: utilisateur => this.estAdmin.set(
        utilisateur.roles.includes('ADMIN')
      ),
      error: () => this.estAdmin.set(false)
    });
  }

  /** Construit un formulaire privé par défaut. */
  private formulaireInitial(date: Date): RendezVousRequest {
    const annee = date.getFullYear();
    const mois = String(date.getMonth() + 1).padStart(2, '0');
    const jour = String(date.getDate()).padStart(2, '0');

    return {
      titre: '',
      date: `${annee}-${mois}-${jour}`,
      heureDebut: '09:00',
      heureFin: '10:00',
      lieu: '',
      description: '',
      partage: false,
      dossierId: null,
      clientId: null
    };
  }

  /** Charge uniquement les rendez-vous autorisés par le backend. */
  chargerRendezVous(): void {
    this.agendaApi.lister().subscribe({
      next: donnees => this.rendezVous.set(donnees),
      error: () => this.erreur.set('Chargement des rendez-vous impossible.')
    });
  }

  /** Retourne les rendez-vous associés à une journée civile. */
  rendezVousDuJour(date: Date): RendezVous[] {
    const cle = this.formulaireInitial(date).date;

    return this.rendezVous()
      .filter(rdv => rdv.date === cle)
      .sort((a, b) => a.heureDebut.localeCompare(b.heureDebut));
  }

  /** Sélectionne un rendez-vous existant pour modification. */
  modifierRendezVous(rdv: RendezVous): void {
    if (!this.estAdmin()) return;
    this.editionId.set(rdv.id);
    this.formulaire = {
      titre: rdv.titre,
      date: rdv.date,
      heureDebut: rdv.heureDebut.slice(0, 5),
      heureFin: rdv.heureFin.slice(0, 5),
      lieu: rdv.lieu,
      description: rdv.description,
      partage: rdv.partage,
      dossierId: rdv.dossierId,
      clientId: rdv.clientId
    };
    this.erreur.set('');
    this.formulaireOuvert.set(true);
  }

  /**
   * Vérifie les données avant envoi.
   * La validation définitive et les autorisations restent côté API.
   */
  /** Confirme puis enregistre le rendez-vous via l'API. */
  async enregistrerRendezVous(): Promise<void> {
    if (!this.estAdmin() || this.enregistrement()) return;

    const donnees = {
      ...this.formulaire,
      titre: this.formulaire.titre.trim()
    };

    if (!donnees.titre || !donnees.date
        || !donnees.heureDebut || !donnees.heureFin
        || donnees.heureFin <= donnees.heureDebut) {
      this.erreur.set('Vérifiez le titre, la date et les horaires.');
      void Swal.fire({
        icon: 'warning',
        title: 'Rendez-vous invalide',
        text: 'Vérifiez le titre, la date et les heures de début et de fin.',
        background: '#151d30',
        color: '#ffffff'
      });
      return;
    }

    const id = this.editionId();
    const conflits = detecterConflits(
      { ...donnees, id },
      this.rendezVous()
    );

    if (conflits.length > 0) {
      const avertissement = await Swal.fire({
        icon: 'warning',
        title: 'Créneau déjà occupé',
        text: conflits.map(rdv =>
          `${rdv.heureDebut.slice(0, 5)}–${rdv.heureFin.slice(0, 5)} : ${rdv.titre}`
        ).join(' ; '),
        showCancelButton: true,
        confirmButtonText: 'Maintenir le créneau',
        cancelButtonText: "Modifier l'horaire",
        background: '#151d30',
        color: '#ffffff'
      });

      if (!avertissement.isConfirmed) return;
    }

    const confirmation = await Swal.fire({
      icon: 'question',
      title: id ? 'Modifier ce rendez-vous ?' : 'Créer ce rendez-vous ?',
      text: donnees.titre,
      showCancelButton: true,
      confirmButtonText: 'Confirmer',
      cancelButtonText: 'Annuler',
      background: '#151d30',
      color: '#ffffff',
      didOpen: () => {
        const container = Swal.getContainer();
        if (container) container.style.zIndex = '30000';
      }
    });

    if (!confirmation.isConfirmed || !this.estAdmin()
        || this.enregistrement()) return;

    this.enregistrement.set(true);
    this.erreur.set('');

    const requete = id
      ? this.agendaApi.modifier(id, donnees)
      : this.agendaApi.creer(donnees);

    requete.subscribe({
      next: rdv => {
        this.rendezVous.update(liste =>
          id
            ? liste.map(element => element.id === id ? rdv : element)
            : [...liste, rdv]
        );

        this.enregistrement.set(false);
        this.fermerFormulaire();

        // Succès affiché uniquement après confirmation du backend.
        void Swal.fire({
          icon: 'success',
          title: id ? 'Rendez-vous modifié' : 'Rendez-vous créé',
          timer: 1800,
          showConfirmButton: false,
          background: '#151d30',
          color: '#ffffff'
        });
      },
      error: () => {
        this.enregistrement.set(false);
        this.erreur.set('Enregistrement impossible.');

        void Swal.fire({
          icon: 'error',
          title: 'Enregistrement impossible',
          text: 'Le serveur a refusé ou échoué à enregistrer le rendez-vous.',
          background: '#151d30',
          color: '#ffffff'
        });
      }
    });
  }

  /** Confirme la suppression avant tout appel à l'API. */
  async supprimerRendezVous(rdv: RendezVous): Promise<void> {
    if (!this.estAdmin()) return;

    const confirmation = await Swal.fire({
      icon: 'warning',
      title: 'Supprimer ce rendez-vous ?',
      text: rdv.titre,
      showCancelButton: true,
      confirmButtonText: 'Supprimer',
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#dc2626',
      background: '#151d30',
      color: '#ffffff',
      didOpen: () => {
        const container = Swal.getContainer();
        if (container) container.style.zIndex = '30000';
      }
    });

    if (!confirmation.isConfirmed || !this.estAdmin()) return;

    this.agendaApi.supprimer(rdv.id).subscribe({
      next: () => {
        this.rendezVous.update(liste =>
          liste.filter(element => element.id !== rdv.id)
        );

        void Swal.fire({
          icon: 'success',
          title: 'Rendez-vous supprimé',
          timer: 1800,
          showConfirmButton: false,
          background: '#151d30',
          color: '#ffffff'
        });
      },
      error: () => {
        this.erreur.set('Suppression impossible.');

        void Swal.fire({
          icon: 'error',
          title: 'Suppression impossible',
          background: '#151d30',
          color: '#ffffff'
        });
      }
    });
  }

  /** Indique si un rendez-vous chevauche un autre rendez-vous. */
  estEnConflit(rdv: RendezVous): boolean {
    return detecterConflits(rdv, this.rendezVous()).length > 0;
  }

  /** Trois prochains rendez-vous à compter d'aujourd'hui. */
  readonly prochainsRendezVous = computed(() => {
    const aujourdHui = this.formulaireInitial(new Date()).date;

    return [...this.rendezVous()]
      .filter(rdv => rdv.date >= aujourdHui)
      .sort((a, b) =>
        a.date.localeCompare(b.date)
        || a.heureDebut.localeCompare(b.heureDebut)
      )
      .slice(0, 3);
  });

  readonly dateCourante = signal(new Date());
  readonly vue = signal<VueAgenda>('mois');
  readonly formulaireOuvert = signal(false);
  readonly dateSelectionnee = signal<Date | null>(null);

  readonly jours = computed(() => {
    const date = this.dateCourante();

    return joursDuMois(
      date.getFullYear(),
      date.getMonth()
    );
  });

  /** Cases vides précédant le premier jour du mois. */
  readonly joursSemaine = computed(() => {
    const date = this.dateCourante();
    const premier = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    premier.setDate(premier.getDate() - ((premier.getDay() + 6) % 7));

    return Array.from({ length: 7 }, (_, index) => {
      const jour = new Date(premier);
      jour.setDate(premier.getDate() + index);
      return jour;
    });
  });

  readonly joursJour = computed(() => {
    const date = this.dateCourante();
    return [new Date(date.getFullYear(), date.getMonth(), date.getDate())];
  });

  readonly casesVides = computed(() => {
    const date = this.dateCourante();
    const nombre = decalagePremierJour(
      date.getFullYear(),
      date.getMonth()
    );

    return Array.from({ length: nombre }, (_, index) => index);
  });

  /** Compare une date civile avec la date du jour. */
  estAujourdhui(date: Date): boolean {
    const maintenant = new Date();

    return date.getFullYear() === maintenant.getFullYear()
      && date.getMonth() === maintenant.getMonth()
      && date.getDate() === maintenant.getDate();
  }

  /** Change de mois sans modifier la date d'origine. */
  changerMois(decalage: number): void {
    this.dateCourante.update(date => {
      if (this.vue() === 'mois') {
        return ajouterMois(date, decalage);
      }

      const resultat = new Date(date);
      resultat.setDate(resultat.getDate() + decalage * (this.vue() === 'semaine' ? 7 : 1));
      return resultat;
    });
  }

  /** Sélectionne la vue demandée. */
  changerVue(vue: VueAgenda): void {
    this.vue.set(vue);
  }

  /** Prépare la création d'un rendez-vous à la date choisie. */
  selectionnerDate(date: Date): void {
    this.dateSelectionnee.set(date);
    this.editionId.set(null);
    this.formulaire = this.formulaireInitial(date);
    this.erreur.set('');
    this.formulaireOuvert.set(true);
  }

  fermerFormulaire(): void {
    this.formulaireOuvert.set(false);
  }
}
