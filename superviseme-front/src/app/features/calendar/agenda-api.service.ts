import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

/**
 * Données nécessaires à la création ou à la modification
 * d'un rendez-vous dans SuperviseMe.
 */
export interface RendezVousRequest {
  titre: string;
  date: string;
  heureDebut: string;
  heureFin: string;
  lieu: string | null;
  description: string | null;
  partage: boolean;
  dossierId: string | null;
  clientId: string | null;
}

/** Rendez-vous enregistré en base de données. */
export interface RendezVous extends RendezVousRequest {
  id: string;
}

/**
 * Centralise les appels HTTP vers l'API Agenda.
 *
 * La sécurité est appliquée par le backend :
 * - ADMIN : consultation et gestion complète.
 * - USER : consultation des rendez-vous partagés uniquement.
 */
@Injectable({ providedIn: 'root' })
export class AgendaApiService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/agenda';

  /** Charge les rendez-vous accessibles à l'utilisateur. */
  lister() {
    return this.http.get<RendezVous[]>(this.url);
  }

  /** Crée un rendez-vous, opération réservée à ADMIN. */
  creer(donnees: RendezVousRequest) {
    return this.http.post<RendezVous>(this.url, donnees);
  }

  /** Modifie un rendez-vous existant, opération réservée à ADMIN. */
  modifier(id: string, donnees: RendezVousRequest) {
    return this.http.put<RendezVous>(
      `${this.url}/${encodeURIComponent(id)}`,
      donnees
    );
  }

  /** Supprime un rendez-vous, opération réservée à ADMIN. */
  supprimer(id: string) {
    return this.http.delete<void>(
      `${this.url}/${encodeURIComponent(id)}`
    );
  }
}
