import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';
import Swal from 'sweetalert2';
import { provideRouter } from '@angular/router';
import { Collaborateurs } from './collaborateurs';
import { UtilisateurApiService } from '../../core/services/utilisateur-api';

describe('Collaborateurs', () => {
  let component: Collaborateurs;
  let fixture: ComponentFixture<Collaborateurs>;
  let api: UtilisateurApiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Collaborateurs],
      providers: [
        provideRouter([]),
        {
          provide: UtilisateurApiService,
          useValue: {
            creer: vi.fn(),
            lister: vi.fn().mockReturnValue(of([])),
            desactiver: vi.fn(),
            reactiver: vi.fn()
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Collaborateurs);
    component = fixture.componentInstance;
    api = TestBed.inject(UtilisateurApiService);
    await fixture.whenStable();
  });

  it('affiche le formulaire de création', () => {
    const element = fixture.nativeElement as HTMLElement;

    component.ouvrirCreation();
    fixture.detectChanges();

    expect(element.querySelector('input[type="email"]')).not.toBeNull();
    expect(element.querySelector('input[type="password"]')).not.toBeNull();
    expect(element.querySelector('button[type="submit"]')).not.toBeNull();
  });

  it('transmet les informations sans choisir de rôle', () => {
    const creer = vi.spyOn(api, 'creer').mockReturnValue(of({
      id: 'uuid-test',
      email: 'collaborateur@example.com',
      role: 'USER', actif: true
    }));

    component.email = 'collaborateur@example.com';
    component.motDePasse = 'MotDePasse123!';
    component.ajouter();

    expect(creer).toHaveBeenCalledWith(
      'collaborateur@example.com',
      'MotDePasse123!'
    );
  });

  it('confirme la création du collaborateur', () => {
    vi.spyOn(api, 'creer').mockReturnValue(of({
      id: 'uuid-test',
      email: 'collaborateur@example.com',
      role: 'USER', actif: true
    }));

    component.email = 'collaborateur@example.com';
    component.motDePasse = 'MotDePasse123!';
    component.ajouter();

    expect(component.succes()).toBeTruthy();
    expect(component.motDePasse).toBe('');
  });

  it('affiche une erreur lorsque la création échoue', () => {
    vi.spyOn(api, 'creer').mockReturnValue(
      throwError(() => new Error('API indisponible'))
    );

    component.email = 'collaborateur@example.com';
    component.motDePasse = 'MotDePasse123!';
    component.ajouter();

    expect(component.erreur()).toBeTruthy();
    expect(component.succes()).toBeFalsy();
    expect(component.chargement()).toBe(false);
  });

  it('empêche une double soumission pendant la création', () => {
    const attente = new Subject<{
      id: string;
      email: string;
      role: string;
      actif: boolean;
    }>();

    const creer = vi.spyOn(api, 'creer')
      .mockReturnValue(attente.asObservable());

    component.email = 'collaborateur@example.com';
    component.motDePasse = 'MotDePasse123!';

    component.ajouter();
    component.ajouter();

    expect(creer).toHaveBeenCalledTimes(1);
    expect(component.chargement()).toBe(true);

    attente.complete();
  });

  it('charge et affiche la liste des collaborateurs', () => {
    component.collaborateurs.set([
      { id: '1', email: 'alice@example.com', role: 'USER', actif: true },
      { id: '2', email: 'admin@example.com', role: 'ADMIN', actif: true }
    ]);
    fixture.detectChanges();

    const texte = fixture.nativeElement.textContent;
    expect(texte).toContain('alice@example.com');
    expect(texte).toContain('admin@example.com');
  });

  it('filtre les collaborateurs par adresse e-mail', () => {
    component.collaborateurs.set([
      { id: '1', email: 'alice@example.com', role: 'USER', actif: true },
      { id: '2', email: 'bob@example.com', role: 'USER', actif: true }
    ]);

    component.recherche.set('alice');

    expect(component.collaborateursFiltres().map(c => c.email))
      .toEqual(['alice@example.com']);
  });

  it('ouvre le formulaire depuis le bouton Nouveau collaborateur', () => {
    fixture.detectChanges();

    const bouton = Array.from(
      fixture.nativeElement.querySelectorAll('button')
    ).find((b: any) => b.textContent.includes('Nouveau collaborateur')) as HTMLButtonElement;

    expect(bouton).toBeDefined();
    bouton.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('input[type="password"]'))
      .not.toBeNull();
  });

  it('permet de revenir à la liste depuis le formulaire', () => {
    component.ouvrirCreation();
    component.annulerCreation();

    expect(component.edition()).toBe(false);
  });


  /** Un compte désactivé reste visible dans la gestion de l'équipe. */
  it('affiche le statut inactif du collaborateur', () => {
    component.collaborateurs.set([
      { id: '1', email: 'ancien@example.com', role: 'USER', actif: false }
    ]);
    component.selectionner(component.collaborateurs()[0]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Compte désactivé');
    expect(fixture.nativeElement.textContent).toContain('ancien@example.com');
  });

  it('confirme la désactivation avec SweetAlert2', async () => {
    const desactiver = vi.spyOn(api, 'desactiver').mockReturnValue(of({
      id: '1', email: 'alice@example.com', role: 'USER', actif: false
    }));
    vi.spyOn(Swal, 'fire').mockResolvedValue({
      isConfirmed: true, isDenied: false, isDismissed: false, value: true
    });

    component.collaborateurs.set([
      { id: '1', email: 'alice@example.com', role: 'USER', actif: true }
    ]);
    component.selectionner(component.collaborateurs()[0]);

    await component.demanderDesactivation();

    expect(desactiver).toHaveBeenCalledWith('1');
    expect(component.collaborateurs()[0].actif).toBe(false);
  });

  it('annule SweetAlert2 sans appel API', async () => {
    const desactiver = vi.spyOn(api, 'desactiver');
    vi.spyOn(Swal, 'fire').mockResolvedValue({
      isConfirmed: false, isDenied: false, isDismissed: true
    });

    component.collaborateurs.set([
      { id: '1', email: 'alice@example.com', role: 'USER', actif: true }
    ]);
    component.selectionner(component.collaborateurs()[0]);

    await component.demanderDesactivation();

    expect(desactiver).not.toHaveBeenCalled();
  });

  /** Réactiver conserve le même collaborateur et son identifiant. */
  it('réactive un collaborateur existant', () => {
    const reactiver = vi.spyOn(api, 'reactiver')
      .mockReturnValue(of({
        id: '1', email: 'ancien@example.com', role: 'USER', actif: true
      }));

    component.collaborateurs.set([
      { id: '1', email: 'ancien@example.com', role: 'USER', actif: false }
    ]);

    component.selectionner(component.collaborateurs()[0]);
    component.reactiverSelection();

    expect(reactiver).toHaveBeenCalledWith('1');
    expect(component.collaborateurs()[0].actif).toBe(true);
    expect(component.collaborateurs()[0].id).toBe('1');
  });

});
