package fr.superviseme.api.realisation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Valide et réencode les images publiques.
 * Aucune métadonnée du fichier d'origine n'est conservée.
 */
@Component
public class ImageSanitizer {

    private static final int MAX_OCTETS = 5 * 1024 * 1024;
    private static final int MAX_DIMENSION = 4096;
    private static final long MAX_PIXELS = 8_000_000L;

    public record ImageValidee(String typeMime, byte[] contenu) {}

    public ImageValidee valider(byte[] contenu) {
        if (contenu == null
                || contenu.length == 0
                || contenu.length > MAX_OCTETS) {
            throw invalide();
        }

        try (var entree = new MemoryCacheImageInputStream(
                new ByteArrayInputStream(contenu))) {

            Iterator<ImageReader> lecteurs = ImageIO.getImageReaders(entree);

            if (!lecteurs.hasNext()) {
                throw invalide();
            }

            ImageReader lecteur = lecteurs.next();

            try {
                lecteur.setInput(entree, true, true);

                String format = lecteur.getFormatName().toLowerCase(
                        java.util.Locale.ROOT);

                if (!format.equals("png")
                        && !format.equals("jpeg")
                        && !format.equals("jpg")) {
                    throw invalide();
                }

                int largeur = lecteur.getWidth(0);
                int hauteur = lecteur.getHeight(0);

                if (largeur <= 0 || hauteur <= 0
                        || largeur > MAX_DIMENSION
                        || hauteur > MAX_DIMENSION
                        || (long) largeur * hauteur > MAX_PIXELS) {
                    throw invalide();
                }

                BufferedImage source = lecteur.read(0);

                if (source == null) {
                    throw invalide();
                }

                boolean png = format.equals("png");
                String sortieFormat = png ? "png" : "jpeg";

                BufferedImage nettoyee = new BufferedImage(
                        largeur,
                        hauteur,
                        png ? BufferedImage.TYPE_INT_ARGB
                                : BufferedImage.TYPE_INT_RGB
                );

                Graphics2D dessin = nettoyee.createGraphics();

                try {
                    dessin.drawImage(source, 0, 0, null);
                } finally {
                    dessin.dispose();
                }

                ByteArrayOutputStream sortie = new ByteArrayOutputStream();

                if (!ImageIO.write(nettoyee, sortieFormat, sortie)) {
                    throw invalide();
                }

                byte[] octets = sortie.toByteArray();

                if (octets.length > MAX_OCTETS) {
                    throw invalide();
                }

                return new ImageValidee(
                        png ? "image/png" : "image/jpeg",
                        octets
                );
            } finally {
                lecteur.dispose();
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw invalide();
        }
    }

    private ResponseStatusException invalide() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Image invalide ou trop volumineuse");
    }
}
