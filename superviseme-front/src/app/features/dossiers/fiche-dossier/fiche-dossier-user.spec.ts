import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { FicheDossier } from './fiche-dossier';
import {
  DossierApi,
  DossierApiService
} from '../../../core/services/dossier-api';
import { ClientApiService } from '../../../core/services/client-api';
import { UtilisateurApiService } from '../../../core/services/utilisateur-api';
import { AuthApiService } from '../../../core/services/auth-api';
import { StatutPersonnaliseApiService } from '../../../core/services/statut-personnalise-api';
import { TacheApiService } from '../../../core/services/tache-api';

describe('FicheDossier - accès collaborateur USER', () => {

  /**
   * Vérifie qu'un collaborateur autorisé peut consulter son dossier
   * sans accéder à l'annuaire global réservé aux administrateurs.
   *
   * Régression ciblée :
   * le chargement de l'annuaire renvoie HTTP 403 pour un USER
   * et ne doit pas provoquer l'échec du chargement du dossier.
   */
  it('charge le dossier sans consulter l’annuaire ADMIN', () => {

    // Dossier fictif dont l'utilisateur connecté est responsable.
    const dossier: DossierApi = {
      id: 'dossier-test',
      titre: 'Chantier autorisé',
      description: null,
      statut: 'EN_COURS',
      statutPersonnaliseId: null,
      priorite: 'NORMALE',
      echeance: null,
      clientId: 'client-test',
      responsableId: 'user-test',
      participantIds: [],
      dateCreation: '',
      dateModification: ''
    };

    // L'API transmet uniquement l'identité du responsable du dossier.
    // Aucun accès à l'annuaire complet n'est nécessaire.
    Object.assign(dossier, {
      responsableNom: 'Alice Martin'
    });

    // Simule un endpoint ADMIN qui refuse l'accès au collaborateur.
    const listerAnnuaire = vi.fn().mockReturnValue(
      throwError(() => ({ status: 403 }))
    );

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: { get: () => dossier.id },
              queryParamMap: { get: () => null }
            }
          }
        },
        {
          provide: DossierApiService,
          useValue: {
            consulter: vi.fn().mockReturnValue(of(dossier))
          }
        },
        {
          provide: ClientApiService,
          useValue: {
            lister: vi.fn().mockReturnValue(of([]))
          }
        },
        {
          provide: UtilisateurApiService,
          useValue: {
            lister: listerAnnuaire
          }
        },
        {
          provide: AuthApiService,
          useValue: {
            moi: vi.fn().mockReturnValue(
              of({ id: 'user-test', roles: ['USER'] })
            )
          }
        },
        {
          provide: StatutPersonnaliseApiService,
          useValue: {
            lister: vi.fn().mockReturnValue(of([]))
          }
        },
        {
          provide: TacheApiService,
          useValue: {
            lister: vi.fn().mockReturnValue(of([]))
          }
        }
      ]
    });

    const fixture = TestBed.createComponent(FicheDossier);
    const composant = fixture.componentInstance;

    // Exécute le chargement initial sans déclencher le rendu du template.
    composant.ngOnInit();

    // Le dossier doit rester consultable malgré la restriction ADMIN.
    expect(composant.dossier()?.titre).toBe('Chantier autorisé');
    expect(composant.erreur()).toBe('');
    expect(composant.chargement()).toBe(false);

    // Un collaborateur USER ne doit pas interroger l'annuaire complet.
    expect(listerAnnuaire).not.toHaveBeenCalled();

    // Le nom doit provenir du dossier et non de l'annuaire ADMIN.
    expect(composant.nomResponsable('user-test'))
      .toBe('Alice Martin');
  });

});
