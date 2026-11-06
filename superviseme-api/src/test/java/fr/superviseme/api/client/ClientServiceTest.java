package fr.superviseme.api.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service de gestion des clients.
 *
 * Vérifie les comportements métier indépendamment de PostgreSQL.
 * Les accès aux données sont simulés avec Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final String NOM_CLIENT = "Dupont";
    private static final String EMAIL_CLIENT = "dupont@example.com";

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private fr.superviseme.api.dossier.DossierRepository dossierRepository;

    private ClientService clientService;

    @BeforeEach
    void initialiserService() {
        clientService = new ClientService(clientRepository, dossierRepository);
    }

    /**
     * Vérifie qu'un client valide est enregistré.
     */
    @Test
    void creerAvecDonneesValidesEnregistreClient() {
        // Arrange
        ClientRequest demande = creerDemande(NOM_CLIENT);

        when(clientRepository.saveAndFlush(any(Client.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ClientResponse resultat = clientService.creer(demande);

        // Assert
        assertEquals(NOM_CLIENT, resultat.nom());
        assertEquals(EMAIL_CLIENT, resultat.email());

        verify(clientRepository).saveAndFlush(any(Client.class));
    }

    /**
     * Vérifie que la liste est obtenue depuis le repository.
     */
    @Test
    void listerSansRechercheRetourneTousLesClients() {
        // Arrange
        Client client = creerClient(NOM_CLIENT);

        when(clientRepository.findAllByOrderByNomAsc())
                .thenReturn(List.of(client));

        // Act
        List<ClientResponse> resultats = clientService.lister(null);

        // Assert
        assertEquals(1, resultats.size());
        assertEquals(NOM_CLIENT, resultats.getFirst().nom());

        verify(clientRepository).findAllByOrderByNomAsc();
    }

    /**
     * Vérifie que les espaces de recherche sont supprimés.
     */
    @Test
    void rechercherParNomSupprimeLesEspacesSuperflus() {
        // Arrange
        String recherche = " dup ";
        String rechercheNormalisee = "dup";

        when(clientRepository.findByNomContainingIgnoreCaseOrderByNomAsc(
                rechercheNormalisee
        )).thenReturn(List.of(creerClient(NOM_CLIENT)));

        // Act
        List<ClientResponse> resultats = clientService.lister(recherche);

        // Assert
        assertEquals(1, resultats.size());

        verify(clientRepository)
                .findByNomContainingIgnoreCaseOrderByNomAsc(
                        rechercheNormalisee
                );
    }

    /**
     * Vérifie qu'un client inexistant déclenche une exception métier.
     */
    @Test
    void consulterClientInexistantDeclencheException() {
        // Arrange
        UUID clientId = UUID.randomUUID();

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ClientIntrouvableException.class,
                () -> clientService.consulter(clientId)
        );
    }

    /**
     * Vérifie que la modification met à jour les données métier.
     */
    @Test
    void modifierClientExistantMetAJourSonNom() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Client client = creerClient("Ancien nom");
        ClientRequest demande = creerDemande("Nouveau nom");

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.of(client));

        when(clientRepository.saveAndFlush(client))
                .thenReturn(client);

        // Act
        ClientResponse resultat = clientService.modifier(clientId, demande);

        // Assert
        assertEquals("Nouveau nom", resultat.nom());
        assertEquals(EMAIL_CLIENT, resultat.email());

        verify(clientRepository).saveAndFlush(client);
    }

    /**
     * Vérifie qu'un client existant est supprimé.
     */
    @Test
    void supprimerClientExistantSupprimeEtSynchronise() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Client client = creerClient(NOM_CLIENT);

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.of(client));

        // Act
        clientService.supprimer(clientId);

        // Assert
        verify(clientRepository).delete(client);
        verify(clientRepository).flush();
    }

    /**
     * Construit une demande valide pour les tests.
     */
    private ClientRequest creerDemande(String nom) {
        return new ClientRequest(
                nom,
                null,
                EMAIL_CLIENT,
                null,
                null
        );
    }

    /**
     * Construit une entité client sans dépendance à JPA.
     */
    private Client creerClient(String nom) {
        return new Client(
                nom,
                null,
                EMAIL_CLIENT,
                null,
                null
        );
    }
}
