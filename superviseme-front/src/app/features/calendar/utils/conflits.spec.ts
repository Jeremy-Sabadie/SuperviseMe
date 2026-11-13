import { describe, expect, it } from 'vitest';
import { detecterConflits } from './conflits';

describe('Conflits Agenda', () => {
  const rdv = (id: string, debut: string, fin: string,
    date = '2026-11-11') => ({
    id, titre: id, date, heureDebut: debut, heureFin: fin
  });

  it('détecte les créneaux identiques', () => {
    expect(detecterConflits(rdv('b', '09:00', '10:00'),
      [rdv('a', '09:00', '10:00')])).toHaveLength(1);
  });

  it('détecte les chevauchements partiels', () => {
    expect(detecterConflits(rdv('b', '09:30', '10:30'),
      [rdv('a', '09:00', '10:00')])).toHaveLength(1);
  });

  it('accepte deux créneaux consécutifs', () => {
    expect(detecterConflits(rdv('b', '10:00', '11:00'),
      [rdv('a', '09:00', '10:00')])).toHaveLength(0);
  });

  it('ignore les autres dates', () => {
    expect(detecterConflits(rdv('b', '09:00', '10:00'),
      [rdv('a', '09:00', '10:00', '2026-11-12')])).toHaveLength(0);
  });

  it('ignore le rendez-vous en cours de modification', () => {
    expect(detecterConflits(rdv('a', '09:00', '10:00'),
      [rdv('a', '09:00', '10:00')])).toHaveLength(0);
  });
});
