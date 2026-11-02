package fr.superviseme.api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Tests de l'authentification des utilisateurs clients. */
class AuthServiceTest {

    private UtilisateurRepository repository;
    private AuthService service;
    private JwtEncoder jwtEncoder;
    private PasswordEncoder encoder;

    @BeforeEach
    void preparer() {
        repository = mock(UtilisateurRepository.class);
        jwtEncoder = mock(JwtEncoder.class);
        encoder = new BCryptPasswordEncoder(4);

        service = new AuthService(repository, encoder, jwtEncoder);
    }

    @Test
    void doitConnecterUnUtilisateurValide() {
        Utilisateur utilisateur = new Utilisateur(
                "patron@exemple.fr",
                encoder.encode("MotDePasseFort2026!"),
                RoleUtilisateur.ADMIN
        );

        // L'ID est normalement attribué par JPA.
        org.springframework.test.util.ReflectionTestUtils.setField(
                utilisateur, "id", UUID.randomUUID());

        when(repository.findByEmail("patron@exemple.fr"))
                .thenReturn(Optional.of(utilisateur));

        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("jwt-test");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        String token = service.connecter(
                "PATRON@EXEMPLE.FR", "MotDePasseFort2026!");

        assertThat(token).isEqualTo("jwt-test");
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void doitRefuserUnMotDePasseIncorrect() {
        Utilisateur utilisateur = new Utilisateur(
                "patron@exemple.fr",
                encoder.encode("MotDePasseFort2026!"),
                RoleUtilisateur.ADMIN
        );

        when(repository.findByEmail("patron@exemple.fr"))
                .thenReturn(Optional.of(utilisateur));

        assertThatThrownBy(() ->
                service.connecter("patron@exemple.fr", "incorrect")
        ).isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtEncoder);
    }

    @Test
    void doitRefuserUnUtilisateurInconnu() {
        when(repository.findByEmail("inconnu@exemple.fr"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.connecter("inconnu@exemple.fr", "MotDePasseFort2026!")
        ).isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtEncoder);
    }
}
