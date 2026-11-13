import { describe, expect, it } from 'vitest';

import {
  genererQrPng,
  urlFormulaire,
  urlPubliqueValide
} from './contact-qr';

describe('QR code du formulaire public', () => {
  it('construit le chemin /contact de l’instance', () => {
    expect(urlFormulaire('https://artisan.example.fr'))
      .toBe('https://artisan.example.fr/contact');
  });

  it('autorise un lien public HTTPS', () => {
    expect(urlPubliqueValide('https://artisan.example.fr/contact'))
      .toBe(true);
  });

  it('refuse localhost et les réseaux privés', () => {
    expect(urlPubliqueValide('http://localhost:4200/contact'))
      .toBe(false);
    expect(urlPubliqueValide('https://192.168.1.5/contact'))
      .toBe(false);
    expect(urlPubliqueValide('https://10.0.0.2/contact'))
      .toBe(false);
  });

  it('refuse les liens HTTP', () => {
    expect(urlPubliqueValide('http://artisan.example.fr/contact'))
      .toBe(false);
  });

  it('génère une image PNG intégrant le lien réel', async () => {
    const png = await genererQrPng(
      'https://artisan.example.fr/contact'
    );

    expect(png.startsWith('data:image/png;base64,')).toBe(true);
  });
});
