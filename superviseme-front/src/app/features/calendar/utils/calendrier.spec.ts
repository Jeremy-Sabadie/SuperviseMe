import { describe, expect, it } from 'vitest';
import {
  ajouterMois,
  debutSemaine,
  joursDuMois
} from './calendrier';

describe('Navigation du calendrier', () => {
  it('passe de décembre à janvier de l’année suivante', () => {
    const resultat = ajouterMois(new Date(2026, 11, 15), 1);

    expect(resultat.getFullYear()).toBe(2027);
    expect(resultat.getMonth()).toBe(0);
  });

  it('évite de sauter février depuis le 31 janvier', () => {
    const resultat = ajouterMois(new Date(2026, 0, 31), 1);

    expect(resultat.getFullYear()).toBe(2026);
    expect(resultat.getMonth()).toBe(1);
  });

  it('commence la semaine le lundi', () => {
    const resultat = debutSemaine(new Date(2026, 10, 15));

    expect(resultat.getFullYear()).toBe(2026);
    expect(resultat.getMonth()).toBe(10);
    expect(resultat.getDate()).toBe(9);
  });

  it('génère les 28 jours de février 2026', () => {
    expect(joursDuMois(2026, 1)).toHaveLength(28);
  });

  it('génère les 29 jours de février 2028', () => {
    expect(joursDuMois(2028, 1)).toHaveLength(29);
  });

  it('génère des dates dans l’ordre chronologique', () => {
    const jours = joursDuMois(2026, 10);

    expect(jours[0].getDate()).toBe(1);
    expect(jours[29].getDate()).toBe(30);
  });
});
