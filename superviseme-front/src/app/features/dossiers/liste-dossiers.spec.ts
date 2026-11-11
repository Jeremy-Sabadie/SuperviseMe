import { StatutPersonnaliseApiService } from '../../core/services/statut-personnalise-api';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { DossierApiService } from '../../core/services/dossier-api';
import { ClientApiService } from '../../core/services/client-api';
import { AuthApiService } from '../../core/services/auth-api';
import { ListeDossiers } from './liste-dossiers';

describe('ListeDossiers', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListeDossiers],
      providers: [
        {
          provide: StatutPersonnaliseApiService,
          useValue: { lister: () => of([]) }
        },

        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthApiService,
          useValue: {
            moi: () => of({ roles: ['ADMIN'] })
          }
        },
        {
          provide: DossierApiService,
          useValue: {
            lister: () => of([
              {
                id: 'dossier-1',
                titre: 'Rénovation escalier',
                statut: 'EN_COURS',
                priorite: 'NORMALE',
                clientId: 'client-1'
              }
            ])
          }
        },
        {
          provide: ClientApiService,
          useValue: {
            lister: () => of([
              { id: 'client-1', nom: 'Dupont', entreprise: 'Menuiserie Dupont' }
            ])
          }
        }
      ]
    }).compileComponents();
  });

  it('affiche les dossiers existants et leur client', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;
    expect(contenu).toContain('Rénovation escalier');
    expect(contenu).toContain('Menuiserie Dupont');
  });

  it('permet de rechercher un dossier', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector(
      'input[type="search"]'
    ) as HTMLInputElement;

    expect(input).toBeTruthy();

    input.value = 'introuvable';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent)
      .not.toContain('Rénovation escalier');
  });

  it('propose un lien vers la création', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const lien = fixture.nativeElement.querySelector(
      'a[href="/dossiers/nouveau"]'
    );

    expect(lien).toBeTruthy();
  });

  it('masque la création aux collaborateurs', () => {
    TestBed.overrideProvider(AuthApiService, {
      useValue: { moi: () => of({ roles: ['USER'] }) }
    });

    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const lien = fixture.nativeElement.querySelector(
      'a[href="/dossiers/nouveau"]'
    );

    expect(lien).toBeNull();
  });



  it('ouvre directement la fiche avec Consulter', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const lien = fixture.nativeElement.querySelector(
      '[data-testid="dossier-consulter"][href="/dossiers/dossier-1"]'
    );

    expect(lien).toBeTruthy();
    expect(lien.textContent).toContain('Consulter');
  });

  it('conserve une action Modifier distincte', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const lien = fixture.nativeElement.querySelector(
      '[data-testid="dossier-modifier"][href="/dossiers/dossier-1?mode=edition"]'
    );

    expect(lien).toBeTruthy();
  });

  it('supprime la fiche intermédiaire', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector(
      '[data-testid="dossier-detail"]'
    )).toBeNull();
    expect(fixture.nativeElement.textContent)
      .not.toContain('Ouvrir la fiche complète');
  });

});
