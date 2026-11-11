import { AuthApiService } from '../../../core/services/auth-api';
import { NgModel } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';
import { AgendaApiService } from '../agenda-api.service';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Calendar } from './calendar';
import Swal from 'sweetalert2';
import { ajouterMois } from '../utils/calendrier';

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
    vi.spyOn(Swal, 'fire').mockResolvedValue({
      isConfirmed: false, isDismissed: true, isDenied: false
    });
    await TestBed.configureTestingModule({
      imports: [Calendar]
    }).compileComponents();

    // Simule un utilisateur ADMIN avant la création du composant.
    vi.spyOn(TestBed.inject(AuthApiService), 'moi').mockReturnValue(
      of({ id: 'admin-test', roles: ['ADMIN'] })
    );

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


describe('Grille mensuelle', () => {
  it('aligne le 1er novembre 2026 sous dimanche', () => {
    const fixture = TestBed.createComponent(Calendar);

    fixture.componentInstance.dateCourante.set(new Date(2026, 10, 1));
    fixture.detectChanges();

    const casesVides = fixture.nativeElement.querySelectorAll(
      '[data-testid="case-vide"]'
    );
    const jours = fixture.nativeElement.querySelectorAll(
      '[data-testid="jour-agenda"]'
    );

    expect(casesVides).toHaveLength(6);
    expect(jours[0].textContent.trim()).toBe('1');
  });
});

describe('Mise en évidence du jour actuel', () => {
  it('identifie le jour actuel dans la grille', () => {
    const maintenant = new Date();
    const fixture = TestBed.createComponent(Calendar);

    fixture.componentInstance.dateCourante.set(maintenant);
    fixture.detectChanges();

    const aujourdHui = fixture.nativeElement.querySelectorAll(
      '[data-testid="aujourdhui"]'
    );

    expect(aujourdHui).toHaveLength(1);
    expect(aujourdHui[0].textContent.trim()).toBe(
      String(maintenant.getDate())
    );
  });

  it('ne marque aucun jour actuel dans un autre mois', () => {
    const fixture = TestBed.createComponent(Calendar);
    const autreMois = ajouterMois(new Date(), 1);

    fixture.componentInstance.dateCourante.set(autreMois);
    fixture.detectChanges();

    const aujourdHui = fixture.nativeElement.querySelectorAll(
      '[data-testid="aujourdhui"]'
    );

    expect(aujourdHui).toHaveLength(0);
  });
});

describe('Vues du calendrier', () => {
  it('affiche sept jours en vue semaine', () => {
    const fixture = TestBed.createComponent(Calendar);
    fixture.componentInstance.dateCourante.set(new Date(2026, 10, 11));
    fixture.componentInstance.changerVue('semaine');
    fixture.detectChanges();

    const jours = fixture.nativeElement.querySelectorAll(
      '[data-testid="jour-agenda"], [data-testid="aujourdhui"]'
    );

    expect(jours).toHaveLength(7);
    expect(fixture.nativeElement.querySelector(
      '[data-testid="agenda-semaine"]'
    )).not.toBeNull();
  });

  it('affiche un seul jour en vue jour', () => {
    const fixture = TestBed.createComponent(Calendar);
    fixture.componentInstance.dateCourante.set(new Date(2026, 10, 11));
    fixture.componentInstance.changerVue('jour');
    fixture.detectChanges();

    const jours = fixture.nativeElement.querySelectorAll(
      '[data-testid="jour-agenda"], [data-testid="aujourdhui"]'
    );

    expect(jours).toHaveLength(1);
    expect(fixture.nativeElement.querySelector(
      '[data-testid="agenda-jour"]'
    )).not.toBeNull();
  });

  it('revient à la grille mensuelle', () => {
    const fixture = TestBed.createComponent(Calendar);
    fixture.componentInstance.changerVue('semaine');
    fixture.componentInstance.changerVue('mois');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector(
      '[data-testid="agenda-mois"]'
    )).not.toBeNull();
  });
});

describe('Création des rendez-vous', () => {
  let fixture: ComponentFixture<Calendar>;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Calendar]
    }).compileComponents();

    // Simule un utilisateur ADMIN avant la création du composant.
    vi.spyOn(TestBed.inject(AuthApiService), 'moi').mockReturnValue(
      of({ id: 'admin-test', roles: ['ADMIN'] })
    );

    fixture = TestBed.createComponent(Calendar);
    element = fixture.nativeElement;
    fixture.componentInstance.dateCourante.set(new Date(2026, 10, 11));
    fixture.detectChanges();
  });

  function remplir(champ: string, valeur: string): void {
    const input = element.querySelector(
      `[data-testid="${champ}"]`
    ) as HTMLInputElement | HTMLTextAreaElement;

    expect(input).not.toBeNull();
    input.value = valeur;
    input.dispatchEvent(new Event('input', { bubbles: true }));
    fixture.detectChanges();
  }

  function ouvrirEtRemplir(): void {
    (element.querySelector('[data-testid="ajouter-rendez-vous"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    remplir('rdv-titre', 'Visite chantier');
    remplir('rdv-date', '2026-11-11');
    remplir('rdv-debut', '09:00');
    remplir('rdv-fin', '10:00');
    remplir('rdv-lieu', 'Bordeaux');
    remplir('rdv-description', 'Rencontre client');
  }

  it('ouvre le formulaire avec le bouton Ajouter', () => {
    (element.querySelector('[data-testid="ajouter-rendez-vous"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(element.querySelector('[data-testid="formulaire-evenement"]')).not.toBeNull();
  });

  it('propose les champs du rendez-vous', () => {
    ouvrirEtRemplir();

    for (const champ of ['rdv-titre', 'rdv-date', 'rdv-debut', 'rdv-fin', 'rdv-lieu', 'rdv-description']) {
      expect(element.querySelector(`[data-testid="${champ}"]`)).not.toBeNull();
    }
  });

  it('refuse un rendez-vous sans titre', () => {
    ouvrirEtRemplir();
    remplir('rdv-titre', '');

    (element.querySelector('[data-testid="enregistrer-rdv"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(fixture.componentInstance.rendezVous()).toHaveLength(0);
  });

  it('refuse une heure de fin antérieure au début', () => {
    ouvrirEtRemplir();
    remplir('rdv-fin', '08:00');

    (element.querySelector('[data-testid="enregistrer-rdv"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(fixture.componentInstance.rendezVous()).toHaveLength(0);
  });

  it('enregistre et affiche un rendez-vous après confirmation API', async () => {
    const confirmation = vi.spyOn(Swal, 'fire')
      .mockResolvedValue({ isConfirmed: true, isDismissed: false, isDenied: false });
    ouvrirEtRemplir();

    // Synchronise les mises à jour ngModel dans l'environnement de test.
    await fixture.whenStable();
    fixture.detectChanges();

    // Met à jour le modèle par le contrôle Angular du champ titre.
    const champTitre = fixture.debugElement.query(
      By.css('[data-testid="rdv-titre"]')
    );
    const controleTitre = champTitre.injector.get(NgModel);
    controleTitre.viewToModelUpdate('Visite chantier');
    fixture.detectChanges();

    // Simule uniquement la réponse du serveur, sans modifier le code métier.
    const api = TestBed.inject(AgendaApiService);
    const creer = vi.spyOn(api, 'creer').mockImplementation(donnees =>
      of({ ...donnees, id: 'rdv-test-001' })
    );

    (element.querySelector('[data-testid="enregistrer-rdv"]') as HTMLButtonElement).click();
    fixture.detectChanges();

    await fixture.whenStable();
    fixture.detectChanges();

    expect(creer).toHaveBeenCalledOnce();
    expect(fixture.componentInstance.rendezVous()).toHaveLength(1);
    expect(element.textContent).toContain('Visite chantier');
    expect(element.querySelector('[data-testid="formulaire-evenement"]')).toBeNull();
    confirmation.mockRestore();
  });
});


/**
 * Vérifie les permissions de l'Agenda selon le rôle de l'utilisateur.
 *
 * ADMIN : peut consulter et gérer les rendez-vous.
 * USER  : peut consulter les rendez-vous autorisés,
 *         mais ne peut pas les créer ou les modifier.
 */
describe('Permissions Agenda', () => {

  /**
   * Prépare un composant Agenda avec un utilisateur authentifié.
   *
   * Le service d'authentification est simulé AVANT la création
   * du composant pour reproduire correctement le chargement du rôle.
   */
  async function creerAgenda(roles: string[]) {
    await TestBed.configureTestingModule({
      imports: [Calendar]
    }).compileComponents();

    // Simule la réponse du backend concernant les rôles.
    vi.spyOn(TestBed.inject(AuthApiService), 'moi').mockReturnValue(
      of({ id: 'utilisateur-test', roles })
    );

    // Création et affichage du composant avec ces permissions.
    const fixture = TestBed.createComponent(Calendar);
    fixture.detectChanges();

    return fixture;
  }

  /**
   * Un utilisateur USER ne doit pas voir le bouton Ajouter.
   * Il ne peut donc pas ouvrir le formulaire par ce bouton.
   */
  it('masque la création pour USER', async () => {
    const fixture = await creerAgenda(['USER']);

    const boutonAjouter = fixture.nativeElement.querySelector(
      '[data-testid="ajouter-rendez-vous"]'
    );

    expect(boutonAjouter).toBeNull();
  });

  /**
   * Vérifie également la protection dans le code TypeScript.
   *
   * Même si une méthode est appelée directement, un USER
   * ne doit pas déclencher une requête de création vers l'API.
   */
  it('interdit la création par appel direct pour USER', async () => {
    const fixture = await creerAgenda(['USER']);
    const api = TestBed.inject(AgendaApiService);

    // Espionne la méthode de création pour détecter tout appel.
    const creer = vi.spyOn(api, 'creer');

    // Prépare un rendez-vous valide pour tester uniquement les droits.
    fixture.componentInstance.formulaire = {
      titre: 'Rendez-vous interdit',
      date: '2026-11-11',
      heureDebut: '09:00',
      heureFin: '10:00',
      lieu: '',
      description: '',
      partage: false,
      dossierId: null,
      clientId: null
    };

    // Tente de créer le rendez-vous malgré le rôle USER.
    fixture.componentInstance.enregistrerRendezVous();

    // Aucune requête de création ne doit être envoyée.
    expect(creer).not.toHaveBeenCalled();
  });

  /**
   * Un administrateur doit conserver son bouton Ajouter.
   * Ce test protège contre une restriction trop importante.
   */
  it('affiche la création pour ADMIN', async () => {
    const fixture = await creerAgenda(['ADMIN']);

    const boutonAjouter = fixture.nativeElement.querySelector(
      '[data-testid="ajouter-rendez-vous"]'
    );

    expect(boutonAjouter).not.toBeNull();
  });
});
