/**
 * Déplace le mois affiché sans modifier la date d'origine.
 * Le premier jour évite les débordements du 29, 30 ou 31.
 */
export function ajouterMois(date: Date, decalage: number): Date {
  return new Date(
    date.getFullYear(),
    date.getMonth() + decalage,
    1
  );
}

/**
 * Retourne le lundi de la semaine contenant la date.
 * En JavaScript, dimanche vaut 0 : on adapte au calendrier français.
 */
export function debutSemaine(date: Date): Date {
  const resultat = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate()
  );

  const decalage = (resultat.getDay() + 6) % 7;
  resultat.setDate(resultat.getDate() - decalage);

  return resultat;
}

/**
 * Génère les jours d'un mois dans l'ordre chronologique.
 * Les dates sont locales et ne passent pas par une conversion UTC.
 */
export function joursDuMois(annee: number, mois: number): Date[] {
  const nombreJours = new Date(annee, mois + 1, 0).getDate();

  return Array.from(
    { length: nombreJours },
    (_, index) => new Date(annee, mois, index + 1)
  );
}
