package fr.superviseme.api.tache;

import fr.superviseme.api.auth.RoleUtilisateur;
import fr.superviseme.api.auth.Utilisateur;
import fr.superviseme.api.auth.UtilisateurRepository;
import fr.superviseme.api.dossier.Dossier;
import fr.superviseme.api.dossier.DossierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TacheServiceTest {

    @Mock
    private TacheRepository repository;

    @Mock
    private UtilisateurRepository utilisateurs;

    @Mock
    private DossierService dossiers;

    @InjectMocks
    private TacheService service;

    @BeforeEach
    void authentifierAdmin() {
        authentifier("ROLE_ADMIN");
    }

    @AfterEach
    void nettoyerSecurite() {
        SecurityContextHolder.clearContext();
    }

    private void authentifier(String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void userNePeutPasCreerTache() {
        authentifier("ROLE_USER");

        assertThrows(AccessDeniedException.class, () ->
                service.creer(new CreerTacheRequest(
                        "Préparer atelier",
                        null,
                        UUID.randomUUID()
                ))
        );

        verifyNoInteractions(repository, utilisateurs, dossiers);
    }

    @Test
    void utilisateurNonAuthentifieNePeutPasCreerTache() {
        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () ->
                service.creer(new CreerTacheRequest(
                        "Préparer atelier",
                        null,
                        UUID.randomUUID()
                ))
        );

        verifyNoInteractions(repository, utilisateurs, dossiers);
    }

    @Test
    void creerTacheIndependante() {
        UUID responsableId = UUID.randomUUID();

        when(utilisateurs.findById(responsableId))
                .thenReturn(Optional.of(utilisateurActif()));

        when(repository.save(any(Tache.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Tache resultat = service.creer(
                new CreerTacheRequest(
                        "Entretenir les machines",
                        null,
                        responsableId
                )
        );

        assertNull(resultat.getDossierId());
        assertEquals(responsableId, resultat.getResponsableId());
        verify(repository).save(any(Tache.class));
    }

    @Test
    void creerTacheLieeADossier() {
        UUID dossierId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();

        when(dossiers.consulter(dossierId))
                .thenReturn(new Dossier("Escalier", null));

        when(utilisateurs.findById(responsableId))
                .thenReturn(Optional.of(utilisateurActif()));

        when(repository.save(any(Tache.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Tache resultat = service.creer(
                new CreerTacheRequest(
                        "Commander le bois",
                        dossierId,
                        responsableId
                )
        );

        assertEquals(dossierId, resultat.getDossierId());
        verify(dossiers).consulter(dossierId);
    }

    @Test
    void refuserCollaborateurDesactive() {
        UUID responsableId = UUID.randomUUID();
        Utilisateur utilisateur = utilisateurActif();
        utilisateur.desactiver();

        when(utilisateurs.findById(responsableId))
                .thenReturn(Optional.of(utilisateur));

        assertThrows(IllegalArgumentException.class, () ->
                service.creer(new CreerTacheRequest(
                        "Préparer atelier",
                        null,
                        responsableId
                ))
        );

        verify(repository, never()).save(any());
    }

    private Utilisateur utilisateurActif() {
        return new Utilisateur(
                "collaborateur@test.local",
                "hash-test",
                RoleUtilisateur.USER
        );
    }
}
