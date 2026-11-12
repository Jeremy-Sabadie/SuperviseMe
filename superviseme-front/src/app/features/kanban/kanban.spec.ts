import Swal from 'sweetalert2';
import { vi } from 'vitest';
import { provideRouter } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { Kanban } from './kanban';

describe('Kanban', () => {
  let component: Kanban;

  beforeEach(() => {
    vi.spyOn(Swal, 'fire').mockResolvedValue({ isConfirmed: false } as any);
    TestBed.configureTestingModule({
      imports: [Kanban], providers: [provideRouter([])]
    });
    component = TestBed.createComponent(Kanban).componentInstance;
    component.dossiers.set([
      { id: '1', titre: 'Dossier 1', client: 'Client A', priorite: 'Normale', responsable: 'Admin', statut: 'A_TRAITER', echeance: null, },
      { id: '2', titre: 'Dossier 2', client: 'Client B', priorite: 'Normale', responsable: 'Admin', statut: 'A_TRAITER', echeance: null, },
      { id: '3', titre: 'Dossier 3', client: 'Client C', priorite: 'Normale', responsable: 'Admin', statut: 'EN_COURS', echeance: null, },
      { id: '4', titre: 'Dossier 4', client: 'Client D', priorite: 'Normale', responsable: 'Admin', statut: 'TERMINE', echeance: null, }
    ]);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('filtre les dossiers par statut', () => {
    expect(component.dossiersParStatut('A_TRAITER').length).toBe(2);
    expect(component.dossiersParStatut('EN_COURS').length).toBe(1);
    expect(component.dossiersParStatut('TERMINE').length).toBe(1);
  });

  it('déplace un dossier vers une autre colonne', () => {
    const source = { data: component.dossiersParStatut('A_TRAITER') };
    const destination = { data: component.dossiersParStatut('EN_COURS') };

    component.deplacer({
      previousContainer: source,
      container: destination,
      previousIndex: 0
    } as CdkDragDrop<any[]> , 'EN_COURS');

    expect(component.dossiers().find(d => d.id === '1')?.statut).toBe('EN_COURS');
  });

  it('ne modifie rien lors d’un déplacement dans la même colonne', () => {
    const avant = component.dossiers();
    const colonne = { data: component.dossiersParStatut('A_TRAITER') };

    component.deplacer({
      previousContainer: colonne,
      container: colonne,
      previousIndex: 0
    } as CdkDragDrop<any[]>, 'A_TRAITER');

    expect(component.dossiers()).toBe(avant);
  });

  it('ouvre directement le formulaire de création depuis le tableau de bord', () => {
    const fixture = TestBed.createComponent(Kanban);
    fixture.componentInstance.estAdmin.set(true);
    fixture.detectChanges();

    const bouton = fixture.nativeElement.querySelector(
      'a.primary[href="/dossiers/nouveau"]'
    );

    expect(bouton).not.toBeNull();
    expect(bouton.textContent.trim()).toBe('+ Nouveau dossier');
  });


  /**
   * Un utilisateur doit pouvoir modifier le statut d'un dossier
   * sans utiliser le glisser-déposer, notamment au clavier.
   */
  it('change le statut depuis une action accessible sur la carte', () => {
    const dossier = component.dossiers()[0];

    component.changerStatutCarte(dossier.id, 'EN_COURS');

    expect(component.dossiers().find(d => d.id === dossier.id)?.statut)
      .toBe('EN_COURS');
  });


});
