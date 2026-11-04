import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private readonly jeton = signal<string | null>(null);

  readonly token = this.jeton.asReadonly();

  definirToken(token: string): void {
    this.jeton.set(token);
  }

  deconnecter(): void {
    this.jeton.set(null);
  }
}
