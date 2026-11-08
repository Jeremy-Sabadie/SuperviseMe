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


  it('affiche le détail du dossier sélectionné sans navigation', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const dossier = fixture.nativeElement.querySelector(
      '[data-testid="dossier-select"]'
    ) as HTMLButtonElement;

    expect(dossier).toBeTruthy();
    dossier.click();
    fixture.detectChanges();

    const detail = fixture.nativeElement.querySelector(
      '[data-testid="dossier-detail"]'
    ) as HTMLElement;

    expect(detail).toBeTruthy();
    expect(detail.textContent).toContain('Rénovation escalier');
    expect(detail.textContent).toContain('Menuiserie Dupont');
    expect(detail.textContent).toContain('EN COURS');
  });

  it('propose un lien vers la fiche complète du dossier sélectionné', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const dossier = fixture.nativeElement.querySelector(
      '[data-testid="dossier-select"]'
    ) as HTMLButtonElement;

    expect(dossier).toBeTruthy();
    dossier.click();
    fixture.detectChanges();

    const lien = fixture.nativeElement.querySelector(
      'a[href="/dossiers/dossier-1"]'
    );

    expect(lien).toBeTruthy();
  });

  it('permet de fermer le détail et de revenir à la liste', () => {
    const fixture = TestBed.createComponent(ListeDossiers);
    fixture.detectChanges();

    const dossier = fixture.nativeElement.querySelector(
      '[data-testid="dossier-select"]'
    ) as HTMLButtonElement;

    expect(dossier).toBeTruthy();
    dossier.click();
    fixture.detectChanges();

    const retour = fixture.nativeElement.querySelector(
      '[data-testid="dossier-retour"]'
    ) as HTMLButtonElement;

    expect(retour).toBeTruthy();
    retour.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector(
      '[data-testid="dossier-detail"]'
    )).toBeNull();
  });

});
