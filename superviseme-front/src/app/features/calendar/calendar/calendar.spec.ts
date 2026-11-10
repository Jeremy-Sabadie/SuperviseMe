import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Calendar } from './calendar';

describe('Agenda', () => {
  let fixture: ComponentFixture<Calendar>;
  let element: HTMLElement;

  /**
   * Recherche un élément par son identifiant de test.
   * Ces identifiants restent indépendants des classes CSS.
   */
  function trouverElement(identifiant: string): HTMLElement | null {
    return element.querySelector(`[data-testid="${identifiant}"]`);
  }

  /**
   * Simule une interaction utilisateur et déclenche
   * la détection des changements Angular.
   */
  function cliquerSur(identifiant: string): void {
    const bouton = trouverElement(identifiant);

    expect(
      bouton,
      `Élément introuvable : ${identifiant}`
    ).not.toBeNull();

    if (!bouton) {
      throw new Error(`Impossible de cliquer sur : ${identifiant}`);
    }

    bouton.click();
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Calendar]
    }).compileComponents();

    fixture = TestBed.createComponent(Calendar);
    element = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();
  });

  describe('Affichage initial', () => {
    it('affiche le titre Agenda', () => {
      expect(element.textContent).toContain('Agenda');
    });

    it('affiche le calendrier mensuel', () => {
      expect(trouverElement('agenda-mois')).not.toBeNull();
    });

    it('affiche les prochaines échéances et rendez-vous', () => {
      expect(trouverElement('agenda-a-venir')).not.toBeNull();
    });
  });

  describe('Navigation', () => {
    it('propose les vues mois, semaine et jour', () => {
      const vues = ['mois', 'semaine', 'jour'];

      for (const vue of vues) {
        expect(trouverElement(`vue-${vue}`)).not.toBeNull();
      }
    });

    it('permet de passer au mois suivant', () => {
      const moisInitial = trouverElement('mois-courant')?.textContent;

      expect(moisInitial).toBeTruthy();

      cliquerSur('mois-suivant');

      const moisSuivant = trouverElement('mois-courant')?.textContent;

      expect(moisSuivant).not.toBe(moisInitial);
    });
  });

  describe('Gestion des rendez-vous', () => {
    it('ouvre le formulaire en sélectionnant une date', () => {
      expect(trouverElement('formulaire-evenement')).toBeNull();

      cliquerSur('jour-agenda');

      expect(trouverElement('formulaire-evenement')).not.toBeNull();
    });
  });
});
