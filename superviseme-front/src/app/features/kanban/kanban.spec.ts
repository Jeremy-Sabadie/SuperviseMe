import { TestBed } from '@angular/core/testing';
import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { Kanban } from './kanban';

describe('Kanban', () => {
  let component: Kanban;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [Kanban]
    });
    component = TestBed.createComponent(Kanban).componentInstance;
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

    expect(component.dossiers().find(d => d.id === 1)?.statut).toBe('EN_COURS');
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
});
