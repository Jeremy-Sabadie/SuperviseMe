package fr.superviseme.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Vérifie les règles métier de création des utilisateurs. */
class UtilisateurMetierTest {

    private UtilisateurRepository repository;
    private UtilisateurService service;
    private List<Utilisateur> utilisateurs;

    @BeforeEach
    void preparer() {
        repository = mock(UtilisateurRepository.class);
        utilisateurs = new ArrayList<>();

        when(repository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur utilisateur = invocation.getArgument(0);
            utilisateurs.add(utilisateur);
            return utilisateur;
        });

        when(repository.count()).thenAnswer(invocation -> (long) utilisateurs.size());

        when(repository.existsByEmail(anyString())).thenAnswer(invocation ->
                utilisateurs.stream().anyMatch(u ->
                        u.getEmail().equals(invocation.getArgument(0))));

        service = new UtilisateurService(
                repository,
                new BCryptPasswordEncoder(4)
        );
    }

    @Test
    void doitCreerUnAdministrateurInitial() {
        Utilisateur utilisateur = service.initialiserAdministrateur(
                "Patron@Exemple.fr", "MotDePasseFort2026!"
        );

        assertThat(utilisateur.getRole()).isEqualTo(RoleUtilisateur.ADMIN);
        assertThat(utilisateur.getEmail()).isEqualTo("patron@exemple.fr");
        assertThat(utilisateur.getMotDePasseHash())
                .isNotEqualTo("MotDePasseFort2026!");
    }

    @Test
    void doitInterdireUnSecondAdministrateurInitial() {
        service.initialiserAdministrateur("patron@exemple.fr", "MotDePasseFort2026!");

        assertThatThrownBy(() ->
                service.initialiserAdministrateur("autre@exemple.fr", "MotDePasseFort2026!")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doitCreerUnCollaborateurAvecRoleUser() {
        Utilisateur utilisateur = service.creerCollaborateur(
                "employe@exemple.fr", "MotDePasseFort2026!"
        );

        assertThat(utilisateur.getRole()).isEqualTo(RoleUtilisateur.USER);
    }

    @Test
    void doitRefuserUnEmailDejaUtilise() {
        service.creerCollaborateur("test@exemple.fr", "MotDePasseFort2026!");

        assertThatThrownBy(() ->
                service.creerCollaborateur("TEST@EXEMPLE.FR", "MotDePasseFort2026!")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void doitRefuserUnMotDePasseTropCourt() {
        assertThatThrownBy(() ->
                service.creerCollaborateur("test@exemple.fr", "123")
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
