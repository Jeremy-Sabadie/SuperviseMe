package fr.superviseme.api.dossier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Vérifie les opérations métier sur les dossiers.
 * Les tests sont isolés de PostgreSQL grâce à Mockito.
 */
@ExtendWith(MockitoExtension.class)
class DossierServiceTest {

    @Mock
    private DossierRepository repository;

    @Mock
    private fr.superviseme.api.client.ClientRepository clientRepository;

    @Mock
    private fr.superviseme.api.auth.UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private DossierService service;

    private UUID dossierId;
    private Dossier dossier;

    @BeforeEach
    void preparer() {
        dossierId = UUID.randomUUID();
        dossier = new Dossier("Installation atelier", "Préparer le matériel");

        // Simule un ADMIN authentifié pour les opérations métier.
        var authentification = new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentification);
    }

    @AfterEach
    void nettoyerSecurite() {
        // Évite toute fuite d'authentification entre les tests.
        SecurityContextHolder.clearContext();
    }

    /** La création doit enregistrer un dossier valide. */
    @Test
    void creerDossier() {
        UUID responsableId = UUID.randomUUID();

        UUID clientId = UUID.randomUUID();

        when(clientRepository.existsById(clientId)).thenReturn(true);
        // Simule un responsable existant dans l'entreprise.
        when(utilisateurRepository.existsById(responsableId)).thenReturn(true);
        when(repository.save(any(Dossier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Dossier resultat = service.creer(
                new CreerDossierRequest(
                        "Installation atelier",
                        "Préparer le matériel",
                        clientId,
                        responsableId)
        );

        assertEquals("Installation atelier", resultat.getTitre());
        assertEquals(StatutDossier.A_TRAITER, resultat.getStatut());
        assertEquals(PrioriteDossier.NORMALE, resultat.getPriorite());
        assertEquals(clientId, resultat.getClientId());
        assertEquals(responsableId, resultat.getResponsableId());

        verify(repository).save(any(Dossier.class));
    }

    @Test
    void creerDossierSansClientRefuse() {
        UUID responsableId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                service.creer(new CreerDossierRequest(
                        "Installation atelier", null, null, responsableId)));

        verify(repository, never()).save(any());
    }

    /** La liste doit retourner les dossiers enregistrés. */
    @Test
    void listerDossiers() {
        when(repository.findAll()).thenReturn(List.of(dossier));

        assertEquals(1, service.lister().size());
        verify(repository).findAll();
    }

    /** Un dossier existant doit être retrouvé par son identifiant. */
    @Test
    void consulterDossier() {
        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));

        assertSame(dossier, service.consulter(dossierId));
    }

    /** Une modification doit mettre à jour les propriétés métier. */
    @Test
    void modifierDossier() {
        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));

        ModifierDossierRequest demande = new ModifierDossierRequest(
                "Atelier terminé",
                "Travaux achevés",
                StatutDossier.TERMINE,
                PrioriteDossier.HAUTE,
                LocalDate.of(2026, 12, 15)
        );

        Dossier resultat = service.modifier(dossierId, demande);

        assertEquals("Atelier terminé", resultat.getTitre());
        assertEquals(StatutDossier.TERMINE, resultat.getStatut());
        assertEquals(PrioriteDossier.HAUTE, resultat.getPriorite());
    }

    /** La suppression doit cibler le dossier demandé. */
    @Test
    void supprimerDossier() {
        when(repository.findById(dossierId)).thenReturn(Optional.of(dossier));

        service.supprimer(dossierId);

        verify(repository).delete(dossier);
    }

    /** Un identifiant inconnu doit provoquer une erreur métier. */
    @Test
    void dossierIntrouvable() {
        when(repository.findById(dossierId)).thenReturn(Optional.empty());

        assertThrows(
                DossierIntrouvableException.class,
                () -> service.consulter(dossierId)
        );
    }

    /** Un dossier sans client peut être associé à un client existant. */
    /** Une association existante ne doit jamais être remplacée. */
    @Test
    void modifierNeDoitPasChangerLeClient() {
        UUID clientInitial = UUID.randomUUID();
        dossier.associerClient(clientInitial);

        when(repository.findById(dossierId))
                .thenReturn(Optional.of(dossier));

        ModifierDossierRequest demande = new ModifierDossierRequest(
                "Nouveau titre",
                "Nouvelle description",
                StatutDossier.EN_COURS,
                PrioriteDossier.HAUTE,
                null
        );

        service.modifier(dossierId, demande);

        assertEquals(clientInitial, dossier.getClientId(),
                "La modification classique ne doit pas changer le client");
    }


    @Test
    void creerDossierAvecEcheance() {
        UUID clientId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();
        LocalDate echeance = LocalDate.of(2027, 1, 15);

        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(utilisateurRepository.existsById(responsableId)).thenReturn(true);
        when(repository.save(any(Dossier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Dossier resultat = service.creer(new CreerDossierRequest(
                "Rénovation escalier",
                "Travaux prévus",
                clientId,
                responsableId,
                echeance
        ));

        assertEquals(echeance, resultat.getEcheance());
        verify(repository).save(any(Dossier.class));
    }

    @Test
    void creerDossierSansEcheance() {
        UUID clientId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();

        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(utilisateurRepository.existsById(responsableId)).thenReturn(true);
        when(repository.save(any(Dossier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Dossier resultat = service.creer(new CreerDossierRequest(
                "Rénovation escalier",
                null,
                clientId,
                responsableId,
                null
        ));

        assertNull(resultat.getEcheance());
        verify(repository).save(any(Dossier.class));
    }


    @Test
    void creerDossierAvecEcheancePasseeRefuse() {
        UUID clientId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();

        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(utilisateurRepository.existsById(responsableId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.creer(new CreerDossierRequest(
                        "Dossier en retard",
                        null,
                        clientId,
                        responsableId,
                        LocalDate.now().minusDays(1)
                ))
        );

        verify(repository, never()).save(any());
    }

    @Test
    void creerDossierAvecEcheanceAujourdhuiAutorise() {
        UUID clientId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();

        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(utilisateurRepository.existsById(responsableId)).thenReturn(true);
        when(repository.save(any(Dossier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Dossier resultat = service.creer(new CreerDossierRequest(
                "Dossier du jour",
                null,
                clientId,
                responsableId,
                LocalDate.now()
        ));

        assertEquals(LocalDate.now(), resultat.getEcheance());
    }


    @Test
    void refuserResponsableDesactivePourNouveauDossier() {
        UUID clientId = UUID.randomUUID();
        UUID responsableId = UUID.randomUUID();

        var responsable = new fr.superviseme.api.auth.Utilisateur(
                "ancien@exemple.fr",
                "hash-test",
                fr.superviseme.api.auth.RoleUtilisateur.USER
        );
        responsable.desactiver();

        when(clientRepository.existsById(clientId)).thenReturn(true);
        when(utilisateurRepository.findById(responsableId))
                .thenReturn(Optional.of(responsable));

        assertThrows(IllegalArgumentException.class, () ->
                service.creer(new CreerDossierRequest(
                        "Nouveau chantier",
                        null,
                        clientId,
                        responsableId
                ))
        );

        verify(repository, never()).save(any());
    }
}
