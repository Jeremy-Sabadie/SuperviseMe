/**
 * Catégories métier des statuts configurables.
 */
export type CategorieStatut =
  | 'CLIENT'
  | 'SIGNATURE_COMMERCIALE'
  | 'APRES_SIGNATURE';

/**
 * Statut personnalisé défini par l'administrateur.
 */
export interface StatutPersonnalise {
  id: string;
  libelle: string;
  couleur: string;
  ordre: number;
  categorie: CategorieStatut;
}
