import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly cle = 'superviseme_access_token';
  private readonly jeton = signal<string | null>(
    sessionStorage.getItem(this.cle)
  );

  readonly token = this.jeton.asReadonly();

  definirToken(token: string): void {
    sessionStorage.setItem(this.cle, token);
    this.jeton.set(token);
  }

  deconnecter(): void {
    sessionStorage.removeItem(this.cle);
    this.jeton.set(null);
  }
}
