import { describe, expect, it } from 'vitest';
import { DossierApi } from '../../../core/services/dossier-api';
import { extraireEcheances } from './echeances';

describe('Extraction des échéances pour l’Agenda', () => {
  /**
   * Construit un dossier représentatif des données de l'API.
   * Les propriétés peuvent être adaptées pour chaque scénario.
   */
  function creerDossier(
    modifications: Partial<DossierApi> = {}
  ): DossierApi {
    return {
      id: 'dossier-1',
      titre: 'Rénovation atelier',
      description: null,
      statut: 'EN_COURS',
      priorite: 'NORMALE',
      echeance: '2026-11-12',
      clientId: null,
      responsableId: null,
      participantIds: [],
      dateCreation: '2026-11-01',
      dateModification: '2026-11-01',
      ...modifications
    };
  }

  it('conserve la date civile sans décalage horaire', () => {
    const echeances = extraireEcheances([
      creerDossier({ echeance: '2026-11-12' })
    ]);

    expect(echeances).toHaveLength(1);
    expect(echeances[0].date).toBe('2026-11-12');
  });

  it('conserve la référence et le titre du dossier', () => {
    const echeances = extraireEcheances([creerDossier()]);

    expect(echeances[0]).toMatchObject({
      dossierId: 'dossier-1',
      titre: 'Rénovation atelier'
    });
  });

  it.each(['TERMINE', 'ARCHIVE'] as const)(
    'exclut les dossiers au statut %s',
    statut => {
      const echeances = extraireEcheances([
        creerDossier({ statut })
      ]);

      expect(echeances).toEqual([]);
    }
  );

  it('ignore les dossiers sans échéance', () => {
    const echeances = extraireEcheances([
      creerDossier({ echeance: null })
    ]);

    expect(echeances).toEqual([]);
  });

  it.each([
    'date-invalide',
    '2026-02-30',
    '2026-13-01',
    '2026-11-00'
  ])('ignore une date invalide : %s', date => {
    const echeances = extraireEcheances([
      creerDossier({ echeance: date })
    ]);

    expect(echeances).toEqual([]);
  });

  it('conserve uniquement les échéances des dossiers actifs', () => {
    const echeances = extraireEcheances([
      creerDossier({ id: 'actif' }),
      creerDossier({ id: 'termine', statut: 'TERMINE' }),
      creerDossier({ id: 'archive', statut: 'ARCHIVE' }),
      creerDossier({ id: 'sans-date', echeance: null })
    ]);

    expect(echeances.map(echeance => echeance.dossierId))
      .toEqual(['actif']);
  });
});
