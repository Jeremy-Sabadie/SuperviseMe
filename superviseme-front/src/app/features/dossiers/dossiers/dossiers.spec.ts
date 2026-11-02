import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Dossiers } from './dossiers';

describe('Dossiers', () => {
  let component: Dossiers;
  let fixture: ComponentFixture<Dossiers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Dossiers],
    }).compileComponents();

    fixture = TestBed.createComponent(Dossiers);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
