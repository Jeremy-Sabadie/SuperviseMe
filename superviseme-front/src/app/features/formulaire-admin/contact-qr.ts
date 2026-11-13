import * as QRCode from 'qrcode';

/**
 * Construit le lien du formulaire hébergé sur l'instance cliente.
 * Le chemin public reste identique sur toutes les instances.
 */
export function urlFormulaire(origin: string): string {
  return new URL('/contact', origin).href;
}

/**
 * Interdit le téléchargement d'un QR destiné à un environnement
 * de développement ou à un réseau privé.
 *
 * Le QR définitif doit utiliser une URL publique HTTPS.
 */
export function urlPubliqueValide(lien: string): boolean {
  try {
    const url = new URL(lien);
    const hote = url.hostname.toLowerCase();

    if (url.protocol !== 'https:') return false;
    if (url.username || url.password) return false;

    if (['localhost', '127.0.0.1', '::1'].includes(hote)) {
      return false;
    }

    if (
      hote.endsWith('.local') ||
      hote.endsWith('.localhost') ||
      hote.endsWith('.test')
    ) {
      return false;
    }

    const parties = hote.split('.').map(Number);

    if (
      parties.length === 4 &&
      parties.every(nombre => Number.isInteger(nombre)
        && nombre >= 0 && nombre <= 255)
    ) {
      const [a, b] = parties;

      // Réseaux privés, loopback et link-local.
      if (
        a === 10 ||
        a === 127 ||
        a === 0 ||
        (a === 172 && b >= 16 && b <= 31) ||
        (a === 192 && b === 168) ||
        (a === 169 && b === 254)
      ) {
        return false;
      }
    }

    return true;
  } catch {
    return false;
  }
}

/**
 * Produit un QR code PNG haute résolution pour l'impression.
 *
 * La correction d'erreur H permet une bonne tolérance aux
 * imperfections d'impression. Les quatre modules de marge
 * assurent la zone blanche nécessaire à la lecture.
 */
export function genererQrPng(lien: string): Promise<string> {
  return QRCode.toDataURL(lien, {
    errorCorrectionLevel: 'H',
    margin: 4,
    width: 1200,
    color: {
      dark: '#101820',
      light: '#FFFFFF'
    }
  });
}
