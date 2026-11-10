import { Component, computed, signal } from '@angular/core';
import { DatePipe, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';

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
  imports: [DatePipe],
  templateUrl: './calendar.html',
  styleUrl: './calendar.scss'
})
export class Calendar {
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
    this.formulaireOuvert.set(true);
  }

  fermerFormulaire(): void {
    this.formulaireOuvert.set(false);
  }
}
