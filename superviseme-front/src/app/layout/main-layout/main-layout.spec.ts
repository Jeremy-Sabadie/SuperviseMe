import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MainLayout } from './main-layout';

describe('MainLayout - refonte UX', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MainLayout],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('affiche les entrées principales de navigation', () => {
    const fixture = TestBed.createComponent(MainLayout);
    fixture.detectChanges();

    const liens = Array.from(
      fixture.nativeElement.querySelectorAll('nav a')
    ) as HTMLAnchorElement[];

    const destinations = liens.map(lien => lien.getAttribute('href'));

    expect(destinations).toContain('/');
    expect(destinations).toContain('/clients');
    expect(destinations).toContain('/dossiers');
    expect(destinations).toContain('/task');
  });

  it('prévoit un emplacement pour le contenu des pages', () => {
    const fixture = TestBed.createComponent(MainLayout);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('router-outlet')
    ).not.toBeNull();
  });
});
