/**
 * Détermine l'initiale de l'avatar à partir du profil utilisateur.
 *
 * Priorité : prénom, puis adresse e-mail.
 * Le caractère '?' indique qu'aucune identité n'est disponible.
 */
export function initialeAvatar(
  prenom: string | null | undefined,
  email: string | null | undefined
): string {
  const identite = prenom?.trim() || email?.trim() || '';

  return identite
    ? Array.from(identite)[0].toLocaleUpperCase('fr-FR')
    : '?';
}
