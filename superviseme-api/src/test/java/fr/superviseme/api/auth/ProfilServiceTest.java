package fr.superviseme.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfilServiceTest {

    private UtilisateurRepository utilisateurs;
    private PasswordEncoder encoder;
    private ProfilService service;
    private Utilisateur compte;
    private UUID id;

    @BeforeEach
    void initialiser() {
        utilisateurs = mock(UtilisateurRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new ProfilService(utilisateurs, encoder);

        compte = new Utilisateur(
                "ancien@example.fr",
                "hash",
                RoleUtilisateur.USER);

        id = UUID.randomUUID();

        when(utilisateurs.findById(id))
                .thenReturn(Optional.of(compte));
    }

    @Test
    void modifierIdentiteSansChangerLeRole() {
        var resultat = service.modifierIdentite(
                id, "  Alice ", " Martin ");

        assertEquals("Alice", resultat.prenom());
        assertEquals("Martin", resultat.nom());
        assertEquals(RoleUtilisateur.USER, resultat.role());
    }

    @Test
    void refuserEmailSansMotDePasseValide() {
        when(encoder.matches("incorrect", "hash"))
                .thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> service.modifierEmail(
                        id, "nouveau@example.fr", "incorrect"));

        assertEquals("ancien@example.fr", compte.getEmail());
    }

    @Test
    void refuserEmailDejaUtilise() {
        when(encoder.matches("correct", "hash"))
                .thenReturn(true);
        when(utilisateurs.existsByEmail("pris@example.fr"))
                .thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> service.modifierEmail(
                        id, "pris@example.fr", "correct"));
    }

    @Test
    void refuserMotDePasseTropCourt() {
        when(encoder.matches("correct", "hash"))
                .thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> service.modifierMotDePasse(
                        id, "correct", "123456"));
    }

    @Test
    void modifierMotDePasseAvecHash() {
        when(encoder.matches("correct", "hash"))
                .thenReturn(true);
        when(encoder.matches("NouveauMotDePasse123!", "hash"))
                .thenReturn(false);
        when(encoder.encode("NouveauMotDePasse123!"))
                .thenReturn("nouveauHash");

        service.modifierMotDePasse(
                id, "correct", "NouveauMotDePasse123!");

        assertEquals("nouveauHash", compte.getMotDePasseHash());
    }

    @Test
    void interdireModificationCompteAbsent() {
        UUID autreId = UUID.randomUUID();

        assertThrows(ResponseStatusException.class,
                () -> service.modifierIdentite(
                        autreId, "Alice", "Martin"));
    }
}
