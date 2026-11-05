import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DossierApi, DossierApiService, PrioriteDossier, StatutDossier } from '../../../core/services/dossier-api';

@Component({
  selector: 'app-fiche-dossier',
  imports: [FormsModule, RouterLink],
  templateUrl: './fiche-dossier.html',
  styleUrl: './fiche-dossier.scss'
})
export class FicheDossier implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(DossierApiService);

  readonly dossier = signal<DossierApi | null>(null);
  readonly erreur = signal('');
  readonly chargement = signal(true);
  readonly enregistrement = signal(false);
  readonly succes = signal(false);

  formulaire = {
    titre: '',
    description: '',
    statut: 'A_TRAITER' as StatutDossier,
    priorite: 'NORMALE' as PrioriteDossier,
    echeance: '',
    clientId: ''
  };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.erreur.set('Identifiant du dossier manquant.');
      this.chargement.set(false);
      return;
    }

    this.api.consulter(id).subscribe({
      next: dossier => {
        this.dossier.set(dossier);
        this.formulaire = {
          titre: dossier.titre,
          description: dossier.description ?? '',
          statut: dossier.statut,
          priorite: dossier.priorite,
          echeance: dossier.echeance ?? '',
          clientId: dossier.clientId ?? ''
        };
        this.chargement.set(false);
      },
      error: () => {
        this.erreur.set('Impossible de charger ce dossier.');
        this.chargement.set(false);
      }
    });
  }

  enregistrer(): void {
    const dossier = this.dossier();
    if (!dossier || !this.formulaire.titre.trim() || this.enregistrement()) return;

    this.enregistrement.set(true);
    this.erreur.set('');
    this.succes.set(false);

    this.api.modifier(dossier.id, {
      titre: this.formulaire.titre.trim(),
      description: this.formulaire.description,
      statut: this.formulaire.statut,
      priorite: this.formulaire.priorite,
      echeance: this.formulaire.echeance || null,
      clientId: this.formulaire.clientId || null
    }).subscribe({
      next: resultat => {
        this.dossier.set(resultat);
        this.enregistrement.set(false);
        this.succes.set(true);
      },
      error: () => {
        this.enregistrement.set(false);
        this.erreur.set('Modification impossible. Vérifiez vos droits.');
      }
    });
  }
}
