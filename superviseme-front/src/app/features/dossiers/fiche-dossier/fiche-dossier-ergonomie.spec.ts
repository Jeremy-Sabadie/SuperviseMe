import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { FicheDossier } from './fiche-dossier';

describe('Fiche dossier - ergonomie', () => {
  function creer() {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    return TestBed.createComponent(FicheDossier).componentInstance;
  }

  it('désactive les modifications si le formulaire est inchangé', () => {
    const fiche = creer();

    fiche.dossier.set({
      id: 'abc12345',
      titre: 'Dossier exemple',
      description: '',
      statut: 'A_TRAITER',
      priorite: 'NORMALE',
      echeance: null,
      clientId: 'client-1',
      responsableId: 'user-1',
      participantIds: [],
      dateCreation: '',
      dateModification: ''
    });

    fiche.formulaire = {
      titre: 'Dossier exemple',
      description: '',
      statut: 'A_TRAITER',
      priorite: 'NORMALE',
      echeance: '',
      clientId: 'client-1'
    };

    expect(fiche.estModifie()).toBe(false);

    fiche.formulaire.titre = 'Nouveau titre';
    expect(fiche.estModifie()).toBe(true);

    fiche.formulaire.titre = 'Dossier exemple';
    expect(fiche.estModifie()).toBe(false);
  });

  it('affiche les informations lisibles du client et du responsable', () => {
    const fiche = creer();

    fiche.clients.set([{
      id: 'client-1',
      nom: 'Dupont',
      entreprise: 'Menuiserie Dupont',
      email: null,
      telephone: null,
      adresse: null,
      dateCreation: '',
      dateModification: ''
    }]);

    fiche.responsables.set([{
      id: 'user-1',
      email: 'responsable@exemple.fr',
      role: 'USER', actif: true
    }]);

    expect(fiche.nomClient('client-1')).toBe('Menuiserie Dupont');
    expect(fiche.nomResponsable('user-1')).toBe('responsable@exemple.fr');
  });
});
