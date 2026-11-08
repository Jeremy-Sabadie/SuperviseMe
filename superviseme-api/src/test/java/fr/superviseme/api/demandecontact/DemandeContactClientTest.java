package fr.superviseme.api.demandecontact;

import fr.superviseme.api.client.Client;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires de l'association entre une demande commerciale
 * et un client du référentiel SuperviseMe.
 *
 * Une demande commerciale reste indépendante d'un dossier.
 */
class DemandeContactClientTest {

    /**
     * Vérifie qu'une demande est associée au client concerné.
     */
    @Test
    void nouvelleDemandeEstAssocieeAuClient() {
        // Arrange
        Client client = new Client(
                "Dupont", null, "dupont@example.com", null, null
        );

        // Act
        DemandeContact demande = new DemandeContact(
                client,
                "Demande de devis",
                "Fabrication d'un escalier"
        );

        // Assert
        assertSame(client, demande.getClient());
        assertEquals("Demande de devis", demande.getMotif());
    }

    /**
     * Vérifie qu'une demande ne peut pas exister sans client associé.
     */
    @Test
    void nouvelleDemandeSansClientEstRefusee() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> new DemandeContact(
                        null,
                        "Demande de devis",
                        "Fabrication d'un escalier"
                )
        );
    }
}
