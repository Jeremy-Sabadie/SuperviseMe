import type { DossierApi } from '../../../core/services/dossier-api';

/** Échéance d'un dossier affichable dans l'Agenda. */
export interface EcheanceAgenda {
  dossierId: string;
  titre: string;
  date: string;
}

/**
 * Vérifie qu'une date respecte le calendrier grégorien.
 * La comparaison se fait sur une date civile, sans conversion UTC.
 */
function estDateValide(valeur: string): boolean {
  const correspondance = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valeur);

  if (!correspondance) {
    return false;
  }

  const annee = Number(correspondance[1]);
  const mois = Number(correspondance[2]);
  const jour = Number(correspondance[3]);

  if (annee < 1 || mois < 1 || mois > 12 || jour < 1) {
    return false;
  }

  // Le calcul explicite évite le comportement particulier
  // de Date.UTC() pour les années comprises entre 0 et 99.
  const bissextile = annee % 4 === 0
    && (annee % 100 !== 0 || annee % 400 === 0);

  const joursParMois = [
    31, bissextile ? 29 : 28, 31, 30,
    31, 30, 31, 31, 30, 31, 30, 31
  ];

  const joursDansMois = joursParMois[mois - 1];

  return jour <= joursDansMois;
}

/**
 * Extrait une date civile ISO.
 * Une éventuelle partie horaire ne doit pas décaler le jour.
 */
function extraireDateCivile(valeur: string | null): string | null {
  if (!valeur) {
    return null;
  }

  const date = valeur.slice(0, 10);

  return estDateValide(date) ? date : null;
}

/** Les dossiers clos ne doivent plus générer d'échéances actives. */
function estDossierActif(dossier: DossierApi): boolean {
  return dossier.statut !== 'TERMINE'
    && dossier.statut !== 'ARCHIVE';
}

/**
 * Transforme les dossiers en échéances affichables.
 * Les dossiers sans date valide ou clôturés sont ignorés.
 */
export function extraireEcheances(
  dossiers: readonly DossierApi[]
): EcheanceAgenda[] {
  return dossiers.flatMap(dossier => {
    if (!estDossierActif(dossier)) {
      return [];
    }

    const date = extraireDateCivile(dossier.echeance);

    if (!date) {
      return [];
    }

    return [{
      dossierId: dossier.id,
      titre: dossier.titre,
      date
    }];
  });
}
