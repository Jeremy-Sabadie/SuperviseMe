import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginResponse {
  accessToken: string;
}

export interface UtilisateurConnecte {
  id: string;
  roles: string[];
}


/** Données publiques du profil, sans information sensible. */
export interface ProfilUtilisateur {
  id: string;
  prenom: string | null;
  nom: string | null;
  email: string;
  role: 'ADMIN' | 'USER';
  actif: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private readonly http = inject(HttpClient);

  connecter(email: string, motDePasse: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', {
      email,
      motDePasse
    });
  }


  /** Récupère les données du compte connecté. */
  profil(): Observable<ProfilUtilisateur> {
    return this.http.get<ProfilUtilisateur>('/api/profil');
  }

  modifierIdentite(
    prenom: string,
    nom: string
  ): Observable<ProfilUtilisateur> {
    return this.http.put<ProfilUtilisateur>(
      '/api/profil/identite',
      { prenom, nom }
    );
  }

  modifierEmail(
    email: string,
    motDePasseActuel: string
  ): Observable<void> {
    return this.http.put<void>(
      '/api/profil/email',
      { email, motDePasseActuel }
    );
  }

  modifierMotDePasse(
    motDePasseActuel: string,
    nouveauMotDePasse: string
  ): Observable<void> {
    return this.http.put<void>(
      '/api/profil/mot-de-passe',
      { motDePasseActuel, nouveauMotDePasse }
    );
  }

  moi(): Observable<UtilisateurConnecte> {
    return this.http.get<UtilisateurConnecte>('/api/auth/me');
  }

}
