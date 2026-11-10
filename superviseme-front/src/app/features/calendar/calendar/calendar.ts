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
  readonly casesVides = computed(() => {
    const date = this.dateCourante();
    const nombre = decalagePremierJour(
      date.getFullYear(),
      date.getMonth()
    );

    return Array.from({ length: nombre }, (_, index) => index);
  });

  /** Change de mois sans modifier la date d'origine. */
  changerMois(decalage: number): void {
    this.dateCourante.update(date => ajouterMois(date, decalage));
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
