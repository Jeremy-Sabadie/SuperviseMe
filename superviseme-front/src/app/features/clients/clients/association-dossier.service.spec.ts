import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom, of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { DossierApi, DossierApiService } from '../../../core/services/dossier-api';
import {
  AssociationDossierService,
  DossierDejaAssocieError
} from './association-dossier.service';

describe('AssociationDossierService', () => {
  const dossier = {
    id: 'd1',
    titre: 'Escalier',
    description: 'Fabrication',
    statut: 'EN_COURS',
    priorite: 'NORMALE',
    echeance: null,
    clientId: 'c1',
    responsableId: 'r1',
    participantIds: [],
    dateCreation: '',
    dateModification: ''
  } as DossierApi;

  const associerClient = vi.fn();
  let service: AssociationDossierService;

  beforeEach(() => {
    vi.resetAllMocks();
    TestBed.configureTestingModule({
      providers: [
        AssociationDossierService,
        {
          provide: DossierApiService,
          useValue: { associerClient }
        }
      ]
    });
    service = TestBed.inject(AssociationDossierService);
  });

  it('associe un dossier par un seul appel API', async () => {
    associerClient.mockReturnValue(of(dossier));

    const resultat = await firstValueFrom(service.associer('d1', 'c1'));

    expect(resultat.clientId).toBe('c1');
    expect(associerClient).toHaveBeenCalledExactlyOnceWith('d1', 'c1');
  });

  it('traduit une réponse HTTP 409 en erreur métier', async () => {
    associerClient.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 409 })
    ));

    await expect(firstValueFrom(service.associer('d1', 'c1')))
      .rejects.toBeInstanceOf(DossierDejaAssocieError);
  });

  it('propage les autres erreurs HTTP', async () => {
    associerClient.mockReturnValue(throwError(() =>
      new HttpErrorResponse({ status: 500 })
    ));

    await expect(firstValueFrom(service.associer('d1', 'c1')))
      .rejects.toMatchObject({ status: 500 });
  });
});
