import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Observable, of } from 'rxjs';
import { TacheApi } from '../../core/services/tache-api';
import { AuthApiService } from '../../core/services/auth-api';
import { TacheApiService } from '../../core/services/tache-api';
import { UtilisateurApiService } from '../../core/services/utilisateur-api';
import { Taches } from './taches';

describe('Taches', () => {
  const taches = [
    {
      id: 'tache-1',
      titre: 'Préparer le devis',
      description: null,
      dossierId: null,
      responsableId: 'user-1',
      statut: 'NON_COMMENCEE' as const,
      priorite: 'NORMALE' as const,
      echeance: null,
      dateCreation: '2026-11-08T10:00:00Z',
      dateModification: '2026-11-08T10:00:00Z',
    },
    {
      id: 'tache-2',
      titre: 'Commander les matériaux',
      description: null,
      dossierId: null,
      responsableId: 'user-2',
      statut: 'EN_COURS' as const,
      priorite: 'HAUTE' as const,
      echeance: null,
      dateCreation: '2026-11-08T10:00:00Z',
      dateModification: '2026-11-08T10:00:00Z',
    },
  ];

  let role = 'USER';
  let changementStatut: ReturnType<typeof vi.fn<(id: string, statut: string) => Observable<TacheApi>>>;

  beforeEach(async () => {
    role = 'USER';
    changementStatut = vi.fn((_id: string, _statut: string) => of(taches[0]));

    await TestBed.configureTestingModule({
      imports: [Taches],
      providers: [
        provideRouter([]),
        {
          provide: AuthApiService,
          useValue: {
            moi: () => of({ id: 'user-1', roles: [role] }),
          },
        },
        {
          provide: TacheApiService,
          useValue: {
            lister: () => of(taches),
            changerStatut: (id: string, statut: string) =>
              changementStatut(id, statut),
            creer: () => of(taches[0]),
          },
        },
        {
          provide: UtilisateurApiService,
          useValue: {
            lister: () => of([
              { id: 'user-1', email: 'alice@test.local', actif: true },
              { id: 'user-2', email: 'bob@test.local', actif: true },
            ]),
          },
        },
      ],
    }).compileComponents();
  });

  it('affiche toutes les tâches aux collaborateurs', () => {
    const fixture = TestBed.createComponent(Taches);
    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;
    expect(contenu).toContain('Préparer le devis');
    expect(contenu).toContain('Commander les matériaux');
  });

  it('masque la création aux collaborateurs', () => {
    const fixture = TestBed.createComponent(Taches);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector(
        '[data-testid="tache-creation"]'
      )
    ).toBeNull();
  });

  it('affiche la création aux administrateurs', () => {
    role = 'ADMIN';

    const fixture = TestBed.createComponent(Taches);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector(
        '[data-testid="tache-creation"]'
      )
    ).toBeTruthy();
  });

  it('autorise le changement de statut de sa tâche', () => {
    const fixture = TestBed.createComponent(Taches);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector(
        '[data-testid="statut-tache-1"]'
      )
    ).toBeTruthy();
  });

  it('interdit le changement de statut de la tâche d’un collègue', () => {
    const fixture = TestBed.createComponent(Taches);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector(
        '[data-testid="statut-tache-2"]'
      )
    ).toBeNull();
  });

  it('autorise ADMIN à changer tous les statuts', () => {
    role = 'ADMIN';

    const fixture = TestBed.createComponent(Taches);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector(
        '[data-testid="statut-tache-1"]'
      )
    ).toBeTruthy();

    expect(
      fixture.nativeElement.querySelector(
        '[data-testid="statut-tache-2"]'
      )
    ).toBeTruthy();
  });
});
