import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Clients } from './clients';
import { provideRouter } from '@angular/router';

describe('Clients', () => {
  let component: Clients;
  let fixture: ComponentFixture<Clients>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Clients],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Clients);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
