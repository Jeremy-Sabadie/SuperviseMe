import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Client {
  id: string;
  nom: string;
  entreprise: string | null;
  email: string | null;
  telephone: string | null;
  adresse: string | null;
  dateCreation: string;
  dateModification: string;
  statutPersonnaliseId: string | null;
}

export type ClientRequest = Pick<
  Client,
  'nom' | 'entreprise' | 'email' | 'telephone' | 'adresse'
>;

@Injectable({ providedIn: 'root' })
export class ClientApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/clients';

  lister(): Observable<Client[]> {
    return this.http.get<Client[]>(this.url);
  }

  consulter(id: string): Observable<Client> {
    return this.http.get<Client>(`${this.url}/${id}`);
  }

  creer(client: ClientRequest): Observable<Client> {
    return this.http.post<Client>(this.url, client);
  }

  supprimer(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  /** Affecte un statut personnalisé à un client. */
  changerStatutPersonnalise(id: string, statutId: string): Observable<Client> {
    return this.http.patch<Client>(
      `${this.url}/${id}/statut-personnalise`,
      { statutId }
    );
  }

  modifier(id: string, client: ClientRequest): Observable<Client> {
    return this.http.put<Client>(`${this.url}/${id}`, client);
  }
}
