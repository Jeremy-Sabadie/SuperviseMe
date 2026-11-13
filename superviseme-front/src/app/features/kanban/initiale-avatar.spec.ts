import { describe, expect, it } from 'vitest';
import { initialeAvatar } from './initiale-avatar';

describe('Initiale de l’avatar', () => {
  it('utilise le prénom en priorité', () => {
    expect(initialeAvatar('Jérémy', 'autre@example.fr')).toBe('J');
  });

  it('utilise l’e-mail si le prénom est absent', () => {
    expect(initialeAvatar(null, 'contact@example.fr')).toBe('C');
  });

  it('ignore les espaces inutiles', () => {
    expect(initialeAvatar('  alice  ', 'autre@example.fr')).toBe('A');
  });

  it('ne produit aucune lettre arbitraire sans identité', () => {
    expect(initialeAvatar(null, null)).toBe('?');
  });
});
