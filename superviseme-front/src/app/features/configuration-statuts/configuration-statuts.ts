import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import Swal from 'sweetalert2';

type Categorie = 'CLIENT' | 'SIGNATURE_COMMERCIALE' | 'APRES_SIGNATURE';

interface Statut {
  id: string;
  categorie: Categorie;
  libelle: string;
  couleur: string;
  ordre: number;
}

/** Configuration des statuts propres à l'entreprise cliente. */
@Component({
  selector: 'app-configuration-statuts',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './configuration-statuts.html',
  styleUrl: './configuration-statuts.scss'
})
export class ConfigurationStatuts implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/admin/statuts';

  readonly categories = [
    { id: 'CLIENT', titre: 'Client' },
    { id: 'SIGNATURE_COMMERCIALE', titre: 'Signature commerciale' },
    { id: 'APRES_SIGNATURE', titre: 'Après signature' }
  ] as const;

  readonly categorie = signal<Categorie>('CLIENT');
  readonly statuts = signal<Statut[]>([]);
  readonly chargement = signal(false);

  libelle = '';
  couleur = '#38bdf8';

  ngOnInit(): void {
    this.charger();
  }

  choisir(categorie: Categorie): void {
    this.categorie.set(categorie);
    this.charger();
  }

  /** Récupère les statuts de la catégorie sélectionnée. */
  charger(): void {
    this.chargement.set(true);
    this.http.get<Statut[]>(this.url, {
      params: { categorie: this.categorie() }
    }).subscribe({
      next: statuts => {
        this.statuts.set([...statuts].sort((a, b) => a.ordre - b.ordre));
        this.chargement.set(false);
      },
      error: () => {
        this.chargement.set(false);
        this.erreur('Impossible de charger les statuts.');
      }
    });
  }

  /** Crée un statut sans modifier les statuts existants. */
  ajouter(): void {
    const libelle = this.libelle.trim();
    if (!libelle || libelle.length > 100) return;

    this.http.post(this.url, {
      categorie: this.categorie(),
      libelle,
      couleur: this.couleur,
      ordre: this.statuts().length
    }).subscribe({
      next: () => {
        this.libelle = '';
        this.charger();
      },
      error: () => this.erreur('Impossible de créer ce statut.')
    });
  }

  /** Modifie le libellé et la couleur d'un statut existant. */
  async modifier(statut: Statut): Promise<void> {
    const resultat = await Swal.fire({
      title: 'Modifier le statut',
      input: 'text',
      inputValue: statut.libelle,
      inputAttributes: { maxlength: '100' },
      showCancelButton: true,
      confirmButtonText: 'Enregistrer',
      cancelButtonText: 'Annuler',
      background: '#151d30',
      color: '#ffffff',
      inputValidator: valeur => !valeur?.trim() ? 'Libellé obligatoire' : null
    });

    if (!resultat.isConfirmed) return;

    this.http.put(`${this.url}/${statut.id}`, {
      libelle: resultat.value.trim(),
      couleur: statut.couleur,
      ordre: statut.ordre
    }).subscribe({
      next: () => this.charger(),
      error: () => this.erreur('Modification impossible.')
    });
  }

  /** Enregistre la nouvelle couleur du statut. */
  changerCouleur(statut: Statut, couleur: string): void {
    this.http.put(`${this.url}/${statut.id}`, {
      libelle: statut.libelle,
      couleur,
      ordre: statut.ordre
    }).subscribe({
      next: () => this.charger(),
      error: () => this.erreur('Impossible de modifier la couleur.')
    });
  }

  /** Change l'ordre d'affichage du statut. */
  deplacer(statut: Statut, direction: number): void {
    const liste = this.statuts();
    const index = liste.findIndex(item => item.id === statut.id);
    const cible = liste[index + direction];
    if (!cible) return;

    this.http.patch(`${this.url}/${statut.id}/ordre`, {
      ordre: cible.ordre
    }).subscribe({
      next: () => this.charger(),
      error: () => this.erreur('Impossible de déplacer ce statut.')
    });
  }

  private erreur(message: string): void {
    void Swal.fire({
      icon: 'error',
      title: 'Erreur',
      text: message,
      background: '#151d30',
      color: '#ffffff'
    });
  }
}
