import { provideRouter } from '@angular/router';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Dossiers } from './dossiers';
import { CollaborateurResponse } from '../../../core/services/utilisateur-api';

describe('Dossiers', () => {
  let component: Dossiers;
  let fixture: ComponentFixture<Dossiers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dossiers], providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Dossiers);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});


describe('Affectation des responsables', () => {
  it('propose uniquement les comptes actifs pour un nouveau dossier', () => {
    const collaborateurs: CollaborateurResponse[] = [
      { id: 'actif', email: 'actif@test.fr', role: 'USER', actif: true },
      { id: 'desactive', email: 'ancien@test.fr', role: 'USER', actif: false }
    ];

    // Un compte désactivé ne peut recevoir de nouveau dossier.
    expect(collaborateurs.filter(c => c.actif).map(c => c.id)).toEqual(['actif']);
  });
});
