import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './contact.html',
  styleUrl: './contact.scss'
})
export class Contact {
  private http = inject(HttpClient);

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
