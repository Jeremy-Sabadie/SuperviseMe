import { Component, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

interface Realisation {
  id: string;
  titre: string;
  description: string | null;
  imageUrl: string;
  position: number;
}

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './contact.html',
  styleUrl: './contact.scss'
})
export class Contact implements OnInit, OnDestroy {
  private http = inject(HttpClient);

  realisations = signal<Realisation[]>([]);
  indexRealisation = signal(0);

  ngOnInit(): void {
    this.http.get<Realisation[]>('/api/public/realisations').subscribe({
      next: data => {
        this.realisations.set(data);
        this.indexRealisation.set(0);
        this.positionPiste.set(data.length > 1 ? 1 : 0);
        this.demarrerCarrousel();
      },
      error: () => this.realisations.set([])
    });
  }

  // Piste avec copies aux extrémités pour une boucle fluide.
  readonly positionPiste = signal(1);
  readonly transitionActive = signal(true);
  readonly animationEnCours = signal(false);

  imagesPiste(): Realisation[] {
    const images = this.realisations();
    return images.length > 1
      ? [images[images.length - 1], ...images, images[0]]
      : images;
  }

  precedente(): void {
    if (this.animationEnCours() || this.realisations().length < 2) return;
    this.animationEnCours.set(true);
    this.positionPiste.update(i => i - 1);
  }

  suivante(): void {
    if (this.animationEnCours() || this.realisations().length < 2) return;
    this.animationEnCours.set(true);
    this.positionPiste.update(i => i + 1);
  }

  finTransition(): void {
    if (!this.animationEnCours()) return;

    const total = this.realisations().length;
    const position = this.positionPiste();

    if (position === 0 || position === total + 1) {
      this.transitionActive.set(false);
      this.positionPiste.set(position === 0 ? total : 1);

      // Réactivation après repositionnement invisible.
      requestAnimationFrame(() => {
        requestAnimationFrame(() => this.transitionActive.set(true));
      });
    }

    this.indexRealisation.set((this.positionPiste() - 1 + total) % total);
    this.animationEnCours.set(false);
  }

  private minuterieCarrousel: ReturnType<typeof setInterval> | null = null;

  demarrerCarrousel(): void {
    this.arreterCarrousel();

    if (this.realisations().length < 2) return;

    this.minuterieCarrousel = setInterval(() => {
      this.suivante();
    }, 4000);
  }

  arreterCarrousel(): void {
    if (this.minuterieCarrousel !== null) {
      clearInterval(this.minuterieCarrousel);
      this.minuterieCarrousel = null;
    }
  }

  ngOnDestroy(): void {
    this.arreterCarrousel();
  }

  nom = '';
  email = '';
  motif = '';
  message = '';
  priseConnaissance = false;

  envoi = signal(false);
  succes = signal(false);
  erreur = signal(false);

  async envoyer() {
    if (this.envoi() || !this.priseConnaissance ||
        !this.nom.trim() || !this.email.trim() ||
        !this.motif || !this.message.trim()) return;

    const confirmation = await Swal.fire({
      icon: 'question',
      title: "Confirmer l'envoi ?",
      text: "Votre demande et vos coordonnées seront transmises à l'entreprise afin qu'elle puisse vous répondre.",
      showCancelButton: true,
      confirmButtonText: "Confirmer l'envoi",
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#8274f5',
      cancelButtonColor: '#475569',
      reverseButtons: true,
      background: '#151d30',
      color: '#ffffff',
      focusCancel: true
    });

    if (!confirmation.isConfirmed || this.envoi()) return;

    this.envoi.set(true);
    this.erreur.set(false);
    this.succes.set(false);

    this.http.post('/api/public/contact', {
      nom: this.nom,
      email: this.email,
      motif: this.motif,
      message: this.message
    }).subscribe({
      next: () => {
        this.envoi.set(false);
        this.succes.set(true);
        void Swal.fire({
          icon: 'success',
          title: 'Demande envoyée !',
          text: 'Votre demande a bien été transmise. Nous vous répondrons prochainement.',
          confirmButtonText: 'Fermer',
          confirmButtonColor: '#8274f5'
        });
        this.nom = '';
        this.email = '';
        this.motif = '';
        this.message = '';
        this.priseConnaissance = false;
      },
      error: () => {
        this.envoi.set(false);
        this.erreur.set(true);
        void Swal.fire({
          icon: 'error',
          title: 'Envoi impossible',
          text: "Votre demande n'a pas pu être envoyée. Veuillez réessayer.",
          confirmButtonText: 'Fermer',
          confirmButtonColor: '#8274f5'
        });
      }
    });
  }
}
