import { of } from 'rxjs';
import { vi } from 'vitest';
import { AuthApiService } from '../../../core/services/auth-api';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Login } from './login';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('affiche les champs de connexion', () => {
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('input[type="email"]')).not.toBeNull();
    expect(element.querySelector('input[type="password"]')).not.toBeNull();
    expect(element.querySelector('button[type="submit"]')).not.toBeNull();
  });


  it('transmet les identifiants au service de connexion', () => {
    const auth = TestBed.inject(AuthApiService);
    const spy = vi.spyOn(auth, 'connecter')
      .mockReturnValue(of({ accessToken: 'jwt-test' }));

    const element = fixture.nativeElement as HTMLElement;
    const email = element.querySelector<HTMLInputElement>('input[type="email"]')!;
    const password = element.querySelector<HTMLInputElement>('input[type="password"]')!;
    const form = element.querySelector<HTMLFormElement>('form')!;

    email.value = 'test@example.com';
    email.dispatchEvent(new Event('input'));
    password.value = 'secret';
    password.dispatchEvent(new Event('input'));

    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));

    expect(spy).toHaveBeenCalledWith('test@example.com', 'secret');
  });

});
