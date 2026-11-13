package fr.superviseme.api.realisation;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ImageSanitizerTest {

    private final ImageSanitizer sanitizer = new ImageSanitizer();

    private byte[] image(String format) throws Exception {
        BufferedImage image = new BufferedImage(
                12, 12, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, sortie));
        return sortie.toByteArray();
    }

    @Test
    void pngValideEstReencodeSansDonneesAjoutees() throws Exception {
        byte[] original = image("png");
        byte[] marqueur = "DONNEE_PRIVEE_TEST".getBytes(
                StandardCharsets.UTF_8);

        byte[] avecDonnees = java.util.Arrays.copyOf(
                original, original.length + marqueur.length);

        System.arraycopy(marqueur, 0, avecDonnees,
                original.length, marqueur.length);

        ImageSanitizer.ImageValidee resultat =
                sanitizer.valider(avecDonnees);

        assertEquals("image/png", resultat.typeMime());
        assertNotNull(ImageIO.read(
                new java.io.ByteArrayInputStream(resultat.contenu())));

        assertFalse(new String(resultat.contenu(),
                StandardCharsets.ISO_8859_1)
                .contains("DONNEE_PRIVEE_TEST"));
    }

    @Test
    void jpegValideEstReencode() throws Exception {
        var resultat = sanitizer.valider(image("jpeg"));

        assertEquals("image/jpeg", resultat.typeMime());
        assertNotNull(ImageIO.read(
                new java.io.ByteArrayInputStream(resultat.contenu())));
    }

    @Test
    void contenuInvalideEstRejete() {
        assertThrows(ResponseStatusException.class,
                () -> sanitizer.valider(
                        new byte[]{(byte) 0xff, (byte) 0xd8,
                                (byte) 0xff, 0x00}));
    }

    @Test
    void contenuVideEstRejete() {
        assertThrows(ResponseStatusException.class,
                () -> sanitizer.valider(new byte[0]));
    }

    @Test
    void fichierTropVolumineuxEstRejete() {
        assertThrows(ResponseStatusException.class,
                () -> sanitizer.valider(new byte[5 * 1024 * 1024 + 1]));
    }
}
