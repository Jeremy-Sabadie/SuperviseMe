export interface Creneau {
  id?: string | null;
  titre: string;
  date: string;
  heureDebut: string;
  heureFin: string;
}

/** Retourne les rendez-vous chevauchant le créneau candidat. */
export function detecterConflits<T extends Creneau>(
  candidat: Creneau,
  existants: readonly T[]
): T[] {
  return existants.filter(rdv =>
    rdv.id !== candidat.id &&
    rdv.date === candidat.date &&
    candidat.heureDebut < rdv.heureFin &&
    rdv.heureDebut < candidat.heureFin
  );
}
