import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { Clients } from './clients';
import { ClientApiService } from '../../../core/services/client-api';
import { DossierApiService } from '../../../core/services/dossier-api';

describe('Clients - Master Detail', () => {
  const clients = [
    { id: 'c1', nom: 'Tutu Test', entreprise: 'Tutu et compagnie',
      email: 'tutu@test.fr', telephone: '0600000000', adresse: 'Pau' },
    { id: 'c2', nom: 'Jean Mouloud', entreprise: 'Rara',
      email: null, telephone: null, adresse: null }
  ];

  const dossiers = [
    { id: 'd1', titre: 'Chantier escalier', clientId: 'c1',
      statut: 'EN_COURS', priorite: 'NORMALE', description: '',
      echeance: null, responsableId: null }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Clients],
      providers: [
        provideRouter([]),
        {
          provide: ClientApiService,
          useValue: { lister: () => of(clients) }
        },
        {
          provide: DossierApiService,
          useValue: { lister: () => of(dossiers) }
        }
      ]
    }).compileComponents();
  });

  it('affiche la liste et la fiche du client sélectionné', () => {
    const fixture = TestBed.createComponent(Clients);
    fixture.detectChanges();

    fixture.componentInstance.selectionner(clients[0] as never);
    fixture.detectChanges();

    const contenu = fixture.nativeElement.textContent;
    expect(contenu).toContain('Tutu Test');
    expect(contenu).toContain('tutu@test.fr');
  });

  it('affiche uniquement les dossiers associés au client', () => {
    const fixture = TestBed.createComponent(Clients);
    fixture.detectChanges();

    fixture.componentInstance.selectionner(clients[0] as never);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent)
      .toContain('Chantier escalier');

    fixture.componentInstance.selectionner(clients[1] as never);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent)
      .not.toContain('Chantier escalier');
  });

  it('propose un accès à la fiche du dossier', () => {
    const fixture = TestBed.createComponent(Clients);
    fixture.detectChanges();

    fixture.componentInstance.selectionner(clients[0] as never);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector(
      'a[href="/dossiers/d1"]'
    )).toBeTruthy();
  });
});
